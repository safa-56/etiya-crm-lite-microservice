package com.etiya.cartservice.business.abstracts;

import com.etiya.cartservice.business.dtos.events.CartItemSagaValidationPayload;

/**
 * Sepete ekleme Saga'sının cart-service (başlatıcı) doğrulama sonucu adımı.
 *
 * <p>product-service'ten gelen sonucu sepet satırının durumuna göre yönlendirir
 * (idempotent): onayda satır ACTIVE olur (ad/fiyat/paket içeriği snapshot'lanır),
 * telafide satır CANCELLED olur (soft-delete). Çağıran (Inbox) transaction'ı içinde
 * çalışır; böylece durum güncellemesi ile inbox kaydı atomik olur.
 */
public interface CartSagaService {

    /** Gelen doğrulama sonucunu ilgili sepet satırına uygular (onay/telafi). */
    void applyValidationResult(CartItemSagaValidationPayload payload);
}
