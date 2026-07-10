package com.etiya.orderservice.business.dtos.events;

/**
 * Saga adım 1 gövdesi: order-service, PENDING bir sipariş için cart-service'ten sepet
 * doğrulaması (satır + toplam snapshot'ı) ister.
 *
 * <p>{@code orderId} saga korelasyon kimliğidir; doğrulama sonucu bu kimlikle geri döner.
 * {@code cartId} doğrulanacak sepettir. {@code eventType}, aynı topic'te akan istek ve
 * sonuç olaylarını ayırt etmek için taşınır.
 */
public record OrderCheckoutRequestedPayload(
        String eventType,
        Long orderId,
        Long cartId
) {
}
