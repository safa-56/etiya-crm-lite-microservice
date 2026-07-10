package com.etiya.orderservice.business.abstracts;

import com.etiya.orderservice.business.dtos.events.OrderCheckoutValidationPayload;

/**
 * Sepetten siparişe geçiş Saga'sının order-service (başlatıcı) doğrulama sonucu adımı.
 *
 * <p>cart-service'ten gelen sonucu siparişin durumuna göre yönlendirir (idempotent):
 * onayda sipariş CONFIRMED olur (satır/toplam/sahiplik snapshot'lanır), telafide sipariş
 * CANCELLED olur (soft-delete). Çağıran (Inbox) transaction'ı içinde çalışır; böylece
 * durum güncellemesi ile inbox kaydı atomik olur.
 */
public interface OrderSagaService {

    /** Gelen doğrulama sonucunu ilgili siparişe uygular (onay/telafi). */
    void applyValidationResult(OrderCheckoutValidationPayload payload);
}
