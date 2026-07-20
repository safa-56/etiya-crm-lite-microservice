package com.etiya.accountservice.business.concretes;

import com.etiya.accountservice.business.abstracts.ProductProjectionService;
import com.etiya.accountservice.business.constants.ProductEvents;
import com.etiya.accountservice.business.dtos.events.ProductEventPayload;
import com.etiya.accountservice.dataAccess.BillingAccountRepository;
import com.etiya.accountservice.entities.BillingAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * {@link ProductProjectionService} uygulaması.
 *
 * <p>product-service olaylarına göre ilgili fatura hesabının
 * {@code activeProductCount} alanını artırır/azaltır. Çağıran (Inbox) transaction'ı
 * içinde çalışır; böylece sayaç güncellemesi ile inbox kaydı atomik olur.
 */
@Service
public class ProductProjectionManager implements ProductProjectionService {

    private static final Logger log = LoggerFactory.getLogger(ProductProjectionManager.class);

    private final BillingAccountRepository billingAccountRepository;

    public ProductProjectionManager(BillingAccountRepository billingAccountRepository) {
        this.billingAccountRepository = billingAccountRepository;
    }

    @Override
    public void applyProductEvent(ProductEventPayload payload) {
        if (payload == null || payload.billingAccountId() == null) {
            log.warn("Ürün olayı hesap kimliği içermiyor, atlanıyor: {}", payload);
            return;
        }

        BillingAccount account = billingAccountRepository
                .findByIdAndDeletedDateIsNull(payload.billingAccountId())
                .orElse(null);
        if (account == null) {
            log.warn("Ürün olayındaki hesap bulunamadı (id={}), atlanıyor.", payload.billingAccountId());
            return;
        }

        int delta = deltaFor(payload.eventType());
        if (delta == 0) {
            log.debug("İlgisiz ürün olay tipi: {}", payload.eventType());
            return;
        }

        int updated = Math.max(0, account.getActiveProductCount() + delta);
        account.setActiveProductCount(updated);
        billingAccountRepository.save(account);
    }

    /** Olay tipine göre sayaç değişimini belirler (+1 aktifleşme, -1 pasifleşme). */
    private int deltaFor(String eventType) {
        if (eventType == null) {
            return 0;
        }
        return switch (eventType) {
            case ProductEvents.PRODUCT_CREATED, ProductEvents.PRODUCT_ACTIVATED -> 1;
            case ProductEvents.PRODUCT_DELETED, ProductEvents.PRODUCT_DEACTIVATED -> -1;
            default -> 0;
        };
    }
}
