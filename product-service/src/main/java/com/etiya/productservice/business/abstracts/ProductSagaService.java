package com.etiya.productservice.business.abstracts;

import com.etiya.productservice.business.dtos.events.ProductSagaValidationPayload;

/**
 * Ürün satışı Saga'sının product-service (başlatıcı) tarafındaki adımı:
 * account-service'ten gelen doğrulama sonucunu uygular (onay ya da telafi).
 */
public interface ProductSagaService {

    /**
     * Doğrulama sonucunu uygular:
     * <ul>
     *   <li>başarılı: PENDING ürünü ACTIVE yapar ve {@code ProductCreated} yayınlar;</li>
     *   <li>başarısız: ürünü CANCELLED yapar (telafi/compensation).</li>
     * </ul>
     * PENDING olmayan ürünlerde idempotent olarak atlanır.
     */
    void applyValidationResult(ProductSagaValidationPayload payload);
}
