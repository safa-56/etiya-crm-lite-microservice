package com.etiya.cartservice.business.abstracts;

import com.etiya.cartservice.business.dtos.events.OrderCheckoutRequestedPayload;

/**
 * Sepetten siparişe geçiş Saga'sının cart-service (doğrulayıcı) adımı.
 *
 * <p>order-service'in gönderdiği sepet doğrulama isteğini alır, sepeti kendi otoriter
 * veritabanından kontrol eder (var/aktif mi, onaylanmış satırı var mı) ve sonucu
 * ({@code OrderCartValidated}/{@code OrderCartValidationFailed}) saga kanalına outbox ile
 * geri yayınlar. Çağıran (Inbox) transaction'ı içinde çalışır.
 */
public interface OrderCheckoutParticipantService {

    /** Gelen sepet doğrulama isteğini işler ve sonucu yayınlar. */
    void handleValidationRequest(OrderCheckoutRequestedPayload request);
}
