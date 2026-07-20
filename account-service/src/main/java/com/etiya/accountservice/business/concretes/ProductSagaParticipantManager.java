package com.etiya.accountservice.business.concretes;

import com.etiya.accountservice.business.abstracts.OutboxService;
import com.etiya.accountservice.business.abstracts.ProductSagaParticipantService;
import com.etiya.accountservice.business.constants.AccountReferenceCodes;
import com.etiya.accountservice.business.constants.Messages;
import com.etiya.accountservice.business.constants.ProductSagaEvents;
import com.etiya.accountservice.business.dtos.events.ProductSagaRequestedPayload;
import com.etiya.accountservice.business.dtos.events.ProductSagaValidationPayload;
import com.etiya.accountservice.dataAccess.BillingAccountRepository;
import com.etiya.accountservice.entities.BillingAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Ürün satışı Saga'sının doğrulayıcı adımıdır (account-service).
 *
 * <p>product-service'in gönderdiği satış isteğini alır, fatura hesabını kendi
 * otoriter veritabanından kontrol eder (aktif ve ACTIVE durumda mı) ve sonucu
 * ({@code ProductAccountValidated}/{@code ProductAccountValidationFailed}) saga
 * kanalına outbox ile geri yayınlar. Çağıran (Inbox) transaction'ı içinde çalışır.
 */
@Service
public class ProductSagaParticipantManager implements ProductSagaParticipantService {

    private static final Logger log = LoggerFactory.getLogger(ProductSagaParticipantManager.class);

    private final BillingAccountRepository billingAccountRepository;
    private final OutboxService outboxService;

    public ProductSagaParticipantManager(BillingAccountRepository billingAccountRepository,
                                         OutboxService outboxService) {
        this.billingAccountRepository = billingAccountRepository;
        this.outboxService = outboxService;
    }

    @Override
    public void handleValidationRequest(ProductSagaRequestedPayload request) {
        if (request == null || request.productId() == null) {
            log.warn("Ürün saga isteği kimlik içermiyor, atlanıyor: {}", request);
            return;
        }

        Long productId = request.productId();
        Long accountId = request.billingAccountId();

        // 1) Fatura hesabı otoriter olarak aktif (silinmemiş) mi?
        BillingAccount account = accountId == null ? null
                : billingAccountRepository.findByIdAndDeletedDateIsNull(accountId).orElse(null);
        if (account == null) {
            publishFailed(productId, accountId, Messages.SAGA_BILLING_ACCOUNT_NOT_FOUND);
            return;
        }

        // 2) Hesap durumu satışa uygun (ACTV) mı?
        if (!AccountReferenceCodes.STATUS_ACTIVE_CODE.equals(account.getGeneralStatus().getShortCode())) {
            publishFailed(productId, accountId, Messages.SAGA_BILLING_ACCOUNT_NOT_ACTIVE);
            return;
        }

        // 3) Başarılı: doğrulandı olayını yayınla.
        publishValidated(productId, accountId);
    }

    private void publishValidated(Long productId, Long accountId) {
        ProductSagaValidationPayload payload = new ProductSagaValidationPayload(
                ProductSagaEvents.ACCOUNT_VALIDATED, productId, accountId, true, null);
        publish(productId, ProductSagaEvents.ACCOUNT_VALIDATED, payload);
        log.info("Ürün saga doğrulandı. productId={}, accountId={}", productId, accountId);
    }

    private void publishFailed(Long productId, Long accountId, String reason) {
        ProductSagaValidationPayload payload = new ProductSagaValidationPayload(
                ProductSagaEvents.ACCOUNT_VALIDATION_FAILED, productId, accountId, false, reason);
        publish(productId, ProductSagaEvents.ACCOUNT_VALIDATION_FAILED, payload);
        log.info("Ürün saga doğrulaması başarısız. productId={}, neden={}", productId, reason);
    }

    /** Sonuç olayını saga kanalına (aggregate=ProductSaga) outbox ile yazar. */
    private void publish(Long productId, String eventType, ProductSagaValidationPayload payload) {
        outboxService.publish(
                ProductSagaEvents.AGGREGATE_TYPE,
                String.valueOf(productId),
                eventType,
                payload);
    }
}
