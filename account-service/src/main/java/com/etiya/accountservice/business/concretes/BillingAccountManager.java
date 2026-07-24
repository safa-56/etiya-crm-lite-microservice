package com.etiya.accountservice.business.concretes;

import com.etiya.accountservice.business.abstracts.BillingAccountService;
import com.etiya.accountservice.business.abstracts.OutboxService;
import com.etiya.accountservice.business.abstracts.ReferenceDataService;
import com.etiya.accountservice.business.constants.AccountEvents;
import com.etiya.accountservice.business.constants.AccountReferenceCodes;
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
import com.etiya.accountservice.entities.enums.AccountType;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

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

    /** Otomatik numara üretiminde benzersiz numara bulunamazsa denenecek üst sınır. */
    private static final int ACCOUNT_NUMBER_MAX_ATTEMPTS = 10;

    private final BillingAccountRepository repository;
    private final BillingAccountMapper mapper;
    private final BillingAccountBusinessRules rules;
    private final OutboxService outboxService;
    private final ReferenceDataService referenceDataService;
    private final SecureRandom random = new SecureRandom();

    public BillingAccountManager(BillingAccountRepository repository,
                                 BillingAccountMapper mapper,
                                 BillingAccountBusinessRules rules,
                                 OutboxService outboxService,
                                 ReferenceDataService referenceDataService) {
        this.repository = repository;
        this.mapper = mapper;
        this.rules = rules;
        this.outboxService = outboxService;
        this.referenceDataService = referenceDataService;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.BILLING_ACCOUNT_LIST, allEntries = true),
            @CacheEvict(value = CacheNames.BILLING_ACCOUNTS_BY_CUSTOMER, allEntries = true)
    })
    public BillingAccountResponse add(CreateBillingAccountRequest request) {
        BillingAccount account = mapper.toEntity(request);

        // Hesap ve sipariş numarası istemciden alınmaz; sistem otomatik üretir.
        // (accountNumber: tam 10 hane ve benzersiz; orderNumber: tam 8 hane.)
        account.setAccountNumber(generateUniqueAccountNumber());
        account.setOrderNumber(generateOrderNumber());

        // --- Saga (choreography) adım 1: hesabı PENDING olarak aç ---
        // Müşteri/adres doğrulaması otoriter olarak customer-service'e bırakılır;
        // hesap, doğrulama sonucu gelene kadar PENDING kalır (henüz kullanılamaz).
        account.setAccountType(AccountType.BILLING_ACCOUNT);
        account.setGeneralStatus(referenceDataService.getStatus(
                AccountReferenceCodes.ENTITY_CUSTOMER_ACCOUNT, AccountReferenceCodes.STATUS_PENDING_CODE));
        account.setActiveProductCount(0);
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
        BillingAccount account = repository.findByIdAndDeletedDateIsNull(id)
                .orElseThrow(() -> new BusinessException(Messages.BILLING_ACCOUNT_NOT_FOUND));
        return mapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.BILLING_ACCOUNT_LIST,
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public PagedResponse<BillingAccountResponse> getAll(Pageable pageable) {
        return PagedResponse.of(
                repository.findAllByDeletedDateIsNull(pageable).map(mapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.BILLING_ACCOUNTS_BY_CUSTOMER, key = "#customerId")
    public List<BillingAccountResponse> getByCustomerId(Long customerId) {
        return repository.findAllByCustomerIdAndDeletedDateIsNull(customerId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @Caching(
            put = @CachePut(value = CacheNames.BILLING_ACCOUNTS, key = "#id"),
            evict = {
                    @CacheEvict(value = CacheNames.BILLING_ACCOUNT_LIST, allEntries = true),
                    @CacheEvict(value = CacheNames.BILLING_ACCOUNTS_BY_CUSTOMER, allEntries = true)
            }
    )
    public BillingAccountResponse update(Long id, UpdateBillingAccountRequest request) {
        BillingAccount account = repository.findByIdAndDeletedDateIsNull(id)
                .orElseThrow(() -> new BusinessException(Messages.BILLING_ACCOUNT_NOT_FOUND));

        // accountNumber/orderNumber sistem-yönetimlidir (oluşturmada üretilir) ve bu ekranda
        // düzenlenmez; güncellemede korunur (istemciden gelen değerlerle üzerine yazılmaz).
        // Adres dışı alanlar senkron güncellenir.
        account.setAccountName(request.accountName());
        account.setAccountDescription(request.accountDescription());

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
            @CacheEvict(value = CacheNames.BILLING_ACCOUNT_LIST, allEntries = true),
            @CacheEvict(value = CacheNames.BILLING_ACCOUNTS_BY_CUSTOMER, allEntries = true)
    })
    public void delete(Long id) {
        BillingAccount account = repository.findByIdAndDeletedDateIsNull(id)
                .orElseThrow(() -> new BusinessException(Messages.BILLING_ACCOUNT_NOT_FOUND));

        // Kabul kriteri: aktif ürünü olan hesap silinemez (kullanıcı Customer
        // Account ekranında kalır; iş hatası döner).
        rules.checkIfBillingAccountHasNoActiveProducts(account);

        // Fiziksel silme yok: durum CUST_ACCT/DEL + deletedDate (soft-delete).
        account.setGeneralStatus(referenceDataService.getStatus(
                AccountReferenceCodes.ENTITY_CUSTOMER_ACCOUNT, AccountReferenceCodes.STATUS_DELETED_CODE));
        account.setDeletedDate(LocalDateTime.now());
        repository.save(account);

        publishEvent(account, AccountEvents.BILLING_ACCOUNT_DELETED);
    }

    // ------------------------------------------------------------------ yardımcılar

    /**
     * Benzersiz bir hesap numarası üretir: <b>tam 10 hane, yalnızca rakam</b>.
     *
     * <p>Üretilen numara veritabanında (soft-delete edilmişler dahil) yoksa döner.
     * Nadir çakışmalarda birkaç kez yeniden dener; benzersizlik ayrıca kolon üzerindeki
     * unique kısıtla da garanti altındadır.
     */
    // TODO: oluşturma biçimini değiştir
    private String generateUniqueAccountNumber() {
        for (int attempt = 0; attempt < ACCOUNT_NUMBER_MAX_ATTEMPTS; attempt++) {
            String candidate = randomDigits(10);
            if (!repository.existsByAccountNumber(candidate)) {
                return candidate;
            }
        }
        throw new BusinessException(Messages.ACCOUNT_NUMBER_GENERATION_FAILED);
    }

    /** Sipariş numarası üretir: <b>tam 8 hane, yalnızca rakam</b>. */
    private String generateOrderNumber() {
        return randomDigits(8);
    }

    /** Baştaki sıfırlar korunacak şekilde {@code length} haneli rakam dizisi üretir. */
    private String randomDigits(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private void publishEvent(BillingAccount account, String eventType) {
        BillingAccountEventPayload payload = new BillingAccountEventPayload(
                account.getId(),
                account.getCustomerId(),
                account.getAccountName(),
                account.getAccountNumber(),
                account.getOrderNumber(),
                account.getGeneralStatus().getShortCode(),
                LocalDateTime.now());
        outboxService.publish(
                AccountEvents.AGGREGATE_TYPE, String.valueOf(account.getId()), eventType, payload);
    }
}
