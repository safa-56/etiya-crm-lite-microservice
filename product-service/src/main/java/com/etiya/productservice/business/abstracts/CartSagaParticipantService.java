package com.etiya.productservice.business.abstracts;

import com.etiya.productservice.business.dtos.events.CartSagaRequestedPayload;

/**
 * Sepet ekleme Saga'sının product-service (doğrulayıcı) adımı.
 *
 * <p>cart-service'ten gelen doğrulama isteğini alır; teklif/kampanyayı kendi otoriter
 * DB'sinden doğrular ve sonucu (ad + fiyat + kampanya içeriği ya da red nedeni) saga
 * kanalına geri yayınlar. Çağıran (Inbox) transaction'ı içinde çalışır.
 */
public interface CartSagaParticipantService {

    /** Gelen doğrulama isteğini işler ve sonucu outbox ile geri yayınlar. */
    void handleValidationRequest(CartSagaRequestedPayload request);
}
