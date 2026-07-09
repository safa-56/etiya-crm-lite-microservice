package com.etiya.accountservice.business.concretes;

import com.etiya.accountservice.business.abstracts.BillingAccountSagaService;
import com.etiya.accountservice.business.abstracts.OutboxService;
import com.etiya.accountservice.business.constants.AccountEvents;
import com.etiya.accountservice.business.dtos.events.BillingAccountEventPayload;
import com.etiya.accountservice.business.dtos.events.BillingAccountSagaValidationPayload;
import com.etiya.accountservice.core.constants.CacheNames;
import com.etiya.accountservice.dataAccess.BillingAccountRepository;
import com.etiya.accountservice.entities.BillingAccount;
import com.etiya.accountservice.entities.enums.AccountStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Saga'nın account-service tarafındaki doğrulama sınıfıdır.
 * Customer-service'ten gelen doğrulama sonucunu alıp hesabı ileri götürür(onay) ya da
 * geri alır(telafi)
 */
@Service
public class BillingAccountSagaManager implements BillingAccountSagaService {

    private static final Logger log = LoggerFactory.getLogger(BillingAccountSagaManager.class);

    private final BillingAccountRepository repository;
    private final OutboxService outboxService;

    public BillingAccountSagaManager(BillingAccountRepository repository,
                                     OutboxService outboxService) {
        this.repository = repository;
        this.outboxService = outboxService;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.BILLING_ACCOUNTS, key = "#payload.billingAccountId"),
            @CacheEvict(value = CacheNames.BILLING_ACCOUNT_LIST, allEntries = true)
    })
    public void applyValidationResult(BillingAccountSagaValidationPayload payload) {
        if (payload == null || payload.billingAccountId() == null) {
            log.warn("Saga doğrulama sonucu kimlik içermiyor, atlanıyor: {}", payload);
            return;
        }

        BillingAccount account = repository.findById(payload.billingAccountId()).orElse(null);
        if (account == null) {
            log.warn("Saga sonucundaki hesap bulunamadı (id={}), atlanıyor.", payload.billingAccountId());
            return;
        }

        // Sonucu hesabın durumuna göre yönlendir (idempotent):
        //  - PENDING           -> oluşturma saga'sı (onay/telafi)
        //  - pendingAddressId  -> adres değişikliği saga'sı (uygula/reddet)
        if (account.getAccountStatus() == AccountStatus.PENDING) {
            if (payload.valid()) {
                confirmCreate(account, payload);
            } else {
                cancelCreate(account, payload);
            }
        } else if (account.getPendingAddressId() != null) {
            if (payload.valid()) {
                applyAddressChange(account, payload);
            } else {
                rejectAddressChange(account, payload);
            }
        } else {
            log.debug("Hesabın bekleyen saga'sı yok (durum={}), sonuç atlanıyor. id={}",
                    account.getAccountStatus(), account.getId());
        }
    }

    // ------------------------------------------------------------ oluşturma saga'sı

    /** Onay: hesabı ACTIVE yapar, otoriter adres snapshot'ını yazar. */
    private void confirmCreate(BillingAccount account, BillingAccountSagaValidationPayload payload) {
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setAddress(formatAddress(payload));
        account.setStatusReason(null);
        repository.save(account);

        publish(account, AccountEvents.BILLING_ACCOUNT_ACTIVATED);
        log.info("Saga onaylandı: hesap ACTIVE. id={}", account.getId());
    }

    /** Telafi (compensation): hesabı CANCELLED yapar ve pasifleştirir. */
    private void cancelCreate(BillingAccount account, BillingAccountSagaValidationPayload payload) {
        account.setAccountStatus(AccountStatus.CANCELLED);
        account.setIsActive(false);
        account.setDeletedDate(LocalDateTime.now());
        account.setStatusReason(payload.reason());
        repository.save(account);

        publish(account, AccountEvents.BILLING_ACCOUNT_CANCELLED);
        log.info("Saga telafi edildi: hesap CANCELLED (neden={}). id={}",
                payload.reason(), account.getId());
    }

    // -------------------------------------------------------- adres değişikliği saga'sı

    /** Onay: bekleyen yeni adresi uygular (addressId + adres metni), beklemeyi temizler. */
    private void applyAddressChange(BillingAccount account, BillingAccountSagaValidationPayload payload) {
        account.setAddressId(account.getPendingAddressId());
        account.setAddress(formatAddress(payload));
        account.setPendingAddressId(null);
        account.setStatusReason(null);
        repository.save(account);

        publish(account, AccountEvents.BILLING_ACCOUNT_UPDATED);
        log.info("Adres değişikliği onaylandı ve uygulandı. id={}, addressId={}",
                account.getId(), account.getAddressId());
    }

    /** Telafi: adres değişikliğini reddeder; eski adres korunur, bekleme temizlenir. */
    private void rejectAddressChange(BillingAccount account, BillingAccountSagaValidationPayload payload) {
        account.setPendingAddressId(null);
        account.setStatusReason(payload.reason());
        repository.save(account);

        log.info("Adres değişikliği reddedildi (neden={}), eski adres korundu. id={}",
                payload.reason(), account.getId());
    }

    private String formatAddress(BillingAccountSagaValidationPayload p) {
        return p.city() + ", " + p.street() + " " + p.houseNumber() + " - " + p.addressDescription();
    }

    private void publish(BillingAccount account, String eventType) {
        outboxService.publish(
                AccountEvents.AGGREGATE_TYPE,
                String.valueOf(account.getId()),
                eventType,
                new BillingAccountEventPayload(
                        account.getId(), account.getCustomerId(), account.getAccountName(),
                        account.getAccountStatus(), LocalDateTime.now()));
    }
}
