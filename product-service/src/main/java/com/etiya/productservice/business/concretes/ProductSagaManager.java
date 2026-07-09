package com.etiya.productservice.business.concretes;

import com.etiya.productservice.business.abstracts.OutboxService;
import com.etiya.productservice.business.abstracts.ProductSagaService;
import com.etiya.productservice.business.constants.ProductEvents;
import com.etiya.productservice.business.dtos.events.ProductEventPayload;
import com.etiya.productservice.business.dtos.events.ProductSagaValidationPayload;
import com.etiya.productservice.core.constants.CacheNames;
import com.etiya.productservice.dataAccess.ProductRepository;
import com.etiya.productservice.entities.Product;
import com.etiya.productservice.entities.enums.ProductStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Ürün satışı Saga'sının product-service (başlatıcı) doğrulama sonucu adımıdır.
 *
 * <p>account-service'ten gelen sonucu ürünün durumuna göre yönlendirir (idempotent):
 * yalnızca PENDING ürünlerde işler. Onayda ürün ACTIVE olur ve {@code ProductCreated}
 * olayı yayınlanır (account-service aktif ürün sayısını buradan günceller); telafide
 * ürün CANCELLED olur. Çağıran (Inbox) transaction'ı içinde çalışır; böylece durum
 * güncellemesi, olay yayını ve inbox kaydı atomik olur.
 */
@Service
public class ProductSagaManager implements ProductSagaService {

    private static final Logger log = LoggerFactory.getLogger(ProductSagaManager.class);

    private final ProductRepository repository;
    private final OutboxService outboxService;

    public ProductSagaManager(ProductRepository repository, OutboxService outboxService) {
        this.repository = repository;
        this.outboxService = outboxService;
    }

    @Override
    @CacheEvict(value = CacheNames.PRODUCTS, key = "#payload.productId")
    public void applyValidationResult(ProductSagaValidationPayload payload) {
        if (payload == null || payload.productId() == null) {
            log.warn("Saga doğrulama sonucu kimlik içermiyor, atlanıyor: {}", payload);
            return;
        }

        Product product = repository.findById(payload.productId()).orElse(null);
        if (product == null) {
            log.warn("Saga sonucundaki ürün bulunamadı (id={}), atlanıyor.", payload.productId());
            return;
        }

        // Idempotency: yalnızca PENDING ürünler ileri götürülür/telafi edilir.
        if (product.getStatus() != ProductStatus.PENDING) {
            log.debug("Ürünün bekleyen saga'sı yok (durum={}), sonuç atlanıyor. id={}",
                    product.getStatus(), product.getId());
            return;
        }

        if (payload.valid()) {
            confirmSale(product);
        } else {
            cancelSale(product, payload.reason());
        }
    }

    /** Onay: ürünü ACTIVE yapar ve ProductCreated olayını yayınlar (sayaç için). */
    private void confirmSale(Product product) {
        product.setStatus(ProductStatus.ACTIVE);
        product.setStatusReason(null);
        repository.save(product);

        outboxService.publish(
                ProductEvents.AGGREGATE_TYPE,
                String.valueOf(product.getId()),
                ProductEvents.PRODUCT_CREATED,
                new ProductEventPayload(product.getId(), product.getAccountId(), ProductEvents.PRODUCT_CREATED));

        log.info("Saga onaylandı: ürün ACTIVE. id={}", product.getId());
    }

    /** Telafi (compensation): ürünü CANCELLED yapar ve pasifleştirir. */
    private void cancelSale(Product product, String reason) {
        product.setStatus(ProductStatus.CANCELLED);
        product.setIsActive(false);
        product.setDeletedDate(LocalDateTime.now());
        product.setStatusReason(reason);
        repository.save(product);

        log.info("Saga telafi edildi: ürün CANCELLED (neden={}). id={}", reason, product.getId());
    }
}
