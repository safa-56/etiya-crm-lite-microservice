package com.etiya.accountservice.business.concretes;

import com.etiya.accountservice.business.abstracts.BillingAccountService;
import com.etiya.accountservice.business.abstracts.OutboxService;
import com.etiya.accountservice.business.constants.AccountEvents;
import com.etiya.accountservice.business.constants.BillingAccountSagaEvents;
import com.etiya.accountservice.business.constants.Messages;
import com.etiya.accountservice.business.dtos.events.BillingAccountEventPayload;
import com.etiya.accountservice.business.dtos.events.BillingAccountSagaRequestedPayload;
import com.etiya.accountservice.business.dtos.requests.CreateBillingAccountRequest;
import com.etiya.accountservice.business.dtos.requests.UpdateBillingAccountRequest;
import com.etiya.accountservice.business.dtos.responses.BillingAccountResponse;
import com.etiya.accountservice.business.dtos.responses.PagedResponse;
import com.etiya.accountservice.business.mappers.BillingAccountMapper;
import com.etiya.accountservice.business.rules.BillingAccountBusinessRules;
import com.etiya.accountservice.core.constants.CacheNames;
import com.etiya.accountservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.accountservice.dataAccess.BillingAccountRepository;
import com.etiya.accountservice.entities.BillingAccount;
import com.etiya.accountservice.entities.enums.AccountStatus;
import com.etiya.accountservice.entities.enums.AccountType;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Fatura hesabı iş mantığı (business/concretes).
 *
 * <p>İş kurallarını {@link BillingAccountBusinessRules}'a, olay yayınını
 * {@link OutboxService}'e delege eder. Kabul kriterleri:
 * <ul>
 *   <li>Oluşturmada {@code accountType=BILLING_ACCOUNT}, {@code accountStatus=ACTIVE}.</li>
 *   <li>Silmede aktif ürün varsa iş hatası; yoksa fiziksel silme yerine soft-delete
 *       (isActive=false + status=PASSIVE).</li>
 *   <li>Listeleme sayfalıdır.</li>
 * </ul>
 */
@Service
public class BillingAccountManager implements BillingAccountService {

    private final BillingAccountRepository repository;
    private final BillingAccountMapper mapper;
    private final BillingAccountBusinessRules rules;
    private final OutboxService outboxService;

    public BillingAccountManager(BillingAccountRepository repository,
                                 BillingAccountMapper mapper,
                                 BillingAccountBusinessRules rules,
                                 OutboxService outboxService) {
        this.repository = repository;
        this.mapper = mapper;
        this.rules = rules;
        this.outboxService = outboxService;
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.BILLING_ACCOUNT_LIST, allEntries = true)
    public BillingAccountResponse add(CreateBillingAccountRequest request) {
        // --- hesap-lokal kural (senkron) ---
        rules.checkIfAccountNumberAlreadyExists(request.accountNumber());

        BillingAccount account = mapper.toEntity(request);

        // --- Saga (choreography) adım 1: hesabı PENDING olarak aç ---
        // Müşteri/adres doğrulaması otoriter olarak customer-service'e bırakılır;
        // hesap, doğrulama sonucu gelene kadar PENDING kalır (henüz kullanılamaz).
        account.setAccountType(AccountType.BILLING_ACCOUNT);
        account.setAccountStatus(AccountStatus.PENDING);
        account.setActiveProductCount(0);
        account.setIsActive(true);
        // Adres metni henüz bilinmiyor; otoriter değer saga doğrulamasında
        // (CustomerValidated) yazılır. Kolon NOT NULL olduğundan boş bırakılır.
        account.setAddress("");

        BillingAccount saved = repository.save(account);

        // --- Saga adım 1 olayı: doğrulama isteği (aynı transaction — outbox) ---
        outboxService.publish(
                BillingAccountSagaEvents.AGGREGATE_TYPE,
                String.valueOf(saved.getId()),
                BillingAccountSagaEvents.CREATION_REQUESTED,
                new BillingAccountSagaRequestedPayload(
                        BillingAccountSagaEvents.CREATION_REQUESTED,
                        saved.getId(), saved.getCustomerId(), saved.getAddressId()));

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.BILLING_ACCOUNTS, key = "#id")
    public BillingAccountResponse getById(Long id) {
        BillingAccount account = repository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException(Messages.BILLING_ACCOUNT_NOT_FOUND));
        return mapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.BILLING_ACCOUNT_LIST,
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public PagedResponse<BillingAccountResponse> getAll(Pageable pageable) {
        return PagedResponse.of(
                repository.findAllByIsActiveTrue(pageable).map(mapper::toResponse));
    }

    @Override
    @Transactional
    @Caching(
            put = @CachePut(value = CacheNames.BILLING_ACCOUNTS, key = "#request.id"),
            evict = @CacheEvict(value = CacheNames.BILLING_ACCOUNT_LIST, allEntries = true)
    )
    public BillingAccountResponse update(Long id, UpdateBillingAccountRequest request) {
        BillingAccount account = repository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException(Messages.BILLING_ACCOUNT_NOT_FOUND));

        // Hesap numarası değiştiyse tekilliği doğrula (hesap-lokal kural).
        if (request.accountNumber() != null
                && !request.accountNumber().equals(account.getAccountNumber())) {
            rules.checkIfAccountNumberAlreadyExists(request.accountNumber());
        }

        // Adres dışı alanlar senkron güncellenir.
        account.setAccountName(request.accountName());
        account.setAccountDescription(request.accountDescription());
        account.setAccountNumber(request.accountNumber());
        account.setOrderNumber(request.orderNumber());

        // Adres değişikliği: otoriter doğrulama Saga ile customer-service'e bırakılır.
        // Eski adres, doğrulama sonucu gelene kadar korunur; yeni adres beklemede tutulur.
        boolean addressChanging = request.addressId() != null
                && !request.addressId().equals(account.getAddressId());
        if (addressChanging) {
            account.setPendingAddressId(request.addressId());
        }

        BillingAccount saved = repository.save(account);

        if (addressChanging) {
            // Saga: yeni adres için doğrulama isteği yayınla (aynı transaction — outbox).
            outboxService.publish(
                    BillingAccountSagaEvents.AGGREGATE_TYPE,
                    String.valueOf(saved.getId()),
                    BillingAccountSagaEvents.ADDRESS_CHANGE_REQUESTED,
                    new BillingAccountSagaRequestedPayload(
                            BillingAccountSagaEvents.ADDRESS_CHANGE_REQUESTED,
                            saved.getId(), saved.getCustomerId(), request.addressId()));
        } else {
            publishEvent(saved, AccountEvents.BILLING_ACCOUNT_UPDATED);
        }

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.BILLING_ACCOUNTS, key = "#id"),
            @CacheEvict(value = CacheNames.BILLING_ACCOUNT_LIST, allEntries = true)
    })
    public void delete(Long id) {
        BillingAccount account = repository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException(Messages.BILLING_ACCOUNT_NOT_FOUND));

        // Kabul kriteri: aktif ürünü olan hesap silinemez (kullanıcı Customer
        // Account ekranında kalır; iş hatası döner).
        rules.checkIfBillingAccountHasNoActiveProducts(account);

        // Fiziksel silme yok: soft-delete + durum PASSIVE.
        account.setIsActive(false);
        account.setAccountStatus(AccountStatus.PASSIVE);
        account.setDeletedDate(LocalDateTime.now());
        repository.save(account);

        publishEvent(account, AccountEvents.BILLING_ACCOUNT_DELETED);
    }

    // ------------------------------------------------------------------ yardımcılar

    private void publishEvent(BillingAccount account, String eventType) {
        BillingAccountEventPayload payload = new BillingAccountEventPayload(
                account.getId(),
                account.getCustomerId(),
                account.getAccountName(),
                account.getAccountNumber(),
                account.getOrderNumber(),
                account.getAccountStatus(),
                LocalDateTime.now());
        outboxService.publish(
                AccountEvents.AGGREGATE_TYPE, String.valueOf(account.getId()), eventType, payload);
    }
}
