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
 * {@link BillingAccountSagaService} uygulaması — Saga'nın account-service adımı.
 *
 * <p>Çağıran (Inbox) transaction'ı içinde çalışır; durum güncellemesi, sonuç
 * olayı (outbox) ve inbox kaydı atomik olur. PENDING olmayan hesaplarda idempotent
 * olarak atlanır (duplicate consume / geç gelen sonuç güvenli).
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

        // İdempotency: yalnızca PENDING hesap saga sonucuyla ilerletilir.
        if (account.getAccountStatus() != AccountStatus.PENDING) {
            log.debug("Hesap PENDING değil (durum={}), saga sonucu atlanıyor. id={}",
                    account.getAccountStatus(), account.getId());
            return;
        }

        if (payload.valid()) {
            confirm(account, payload);
        } else {
            compensate(account, payload);
        }
    }

    /** Onay: hesabı ACTIVE yapar, otoriter adres snapshot'ını yazar. */
    private void confirm(BillingAccount account, BillingAccountSagaValidationPayload payload) {
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setAddress(formatAddress(payload));
        account.setStatusReason(null);
        repository.save(account);

        publish(account, AccountEvents.BILLING_ACCOUNT_ACTIVATED);
        log.info("Saga onaylandı: hesap ACTIVE. id={}", account.getId());
    }

    /** Telafi (compensation): hesabı CANCELLED yapar ve pasifleştirir. */
    private void compensate(BillingAccount account, BillingAccountSagaValidationPayload payload) {
        account.setAccountStatus(AccountStatus.CANCELLED);
        account.setIsActive(false);
        account.setDeletedDate(LocalDateTime.now());
        account.setStatusReason(payload.reason());
        repository.save(account);

        publish(account, AccountEvents.BILLING_ACCOUNT_CANCELLED);
        log.info("Saga telafi edildi: hesap CANCELLED (neden={}). id={}",
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
