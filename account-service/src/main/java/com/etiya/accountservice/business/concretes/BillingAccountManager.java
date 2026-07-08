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
import com.etiya.accountservice.dataAccess.CustomerAddressProjectionRepository;
import com.etiya.accountservice.entities.BillingAccount;
import com.etiya.accountservice.entities.enums.AccountStatus;
import com.etiya.accountservice.entities.enums.AccountType;
import com.etiya.accountservice.entities.projection.CustomerAddressProjection;
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
    private final CustomerAddressProjectionRepository addressProjectionRepository;

    public BillingAccountManager(BillingAccountRepository repository,
                                 BillingAccountMapper mapper,
                                 BillingAccountBusinessRules rules,
                                 OutboxService outboxService,
                                 CustomerAddressProjectionRepository addressProjectionRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.rules = rules;
        this.outboxService = outboxService;
        this.addressProjectionRepository = addressProjectionRepository;
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
        account.setAddress(null); // otoriter adres snapshot'ı doğrulamada gelecek

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
    public BillingAccountResponse update(UpdateBillingAccountRequest request) {
        BillingAccount account = repository.findByIdAndIsActiveTrue(request.id())
                .orElseThrow(() -> new BusinessException(Messages.BILLING_ACCOUNT_NOT_FOUND));

        // Seçilen (yeni) adres, hesabın müşterisine ait olmalı; metni projeksiyondan çöz.
        CustomerAddressProjection address = resolveCustomerAddress(
                account.getCustomerId(), request.addressId());

        // Hesap numarası değiştiyse tekilliği doğrula.
        if (request.accountNumber() != null
                && !request.accountNumber().equals(account.getAccountNumber())) {
            rules.checkIfAccountNumberAlreadyExists(request.accountNumber());
        }

        account.setAccountName(request.accountName());
        account.setAccountDescription(request.accountDescription());
        account.setAddressId(request.addressId());
        account.setAddress(formatAddress(address));
        account.setAccountNumber(request.accountNumber());
        account.setOrderNumber(request.orderNumber());

        BillingAccount saved = repository.save(account);
        publishEvent(saved, AccountEvents.BILLING_ACCOUNT_UPDATED);

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

    /**
     * Seçilen adresin ({@code addressId}) verilen müşteriye ait olduğunu yerel
     * projeksiyondan (Kafka read-model) doğrular ve projeksiyon kaydını döner.
     * Adres müşteriye ait değilse/bulunamazsa iş hatası fırlatılır.
     */
    private CustomerAddressProjection resolveCustomerAddress(Long customerId, Long addressId) {
        return addressProjectionRepository.findByAddressIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new BusinessException(Messages.ADDRESS_NOT_FOUND_FOR_CUSTOMER));
    }

    /** Adres projeksiyonunu hesapta saklanacak okunur metne (snapshot) dönüştürür. */
    private String formatAddress(CustomerAddressProjection address) {
        return address.getCity() + ", " + address.getStreet() + " "
                + address.getHouseNumber() + " - " + address.getAddressDescription();
    }

    private void publishEvent(BillingAccount account, String eventType) {
        BillingAccountEventPayload payload = new BillingAccountEventPayload(
                account.getId(),
                account.getCustomerId(),
                account.getAccountName(),
                account.getAccountStatus(),
                LocalDateTime.now());
        outboxService.publish(
                AccountEvents.AGGREGATE_TYPE, String.valueOf(account.getId()), eventType, payload);
    }
}
