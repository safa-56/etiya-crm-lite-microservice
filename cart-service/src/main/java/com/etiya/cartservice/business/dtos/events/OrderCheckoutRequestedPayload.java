package com.etiya.cartservice.business.dtos.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Sepetten siparişe geçiş Saga'sı adım 1 gövdesi (cart-service doğrulayıcı tarafında
 * <b>tüketilir</b>): order-service, PENDING bir sipariş için sepet doğrulaması ister.
 *
 * <p>{@code orderId} saga korelasyon kimliğidir; doğrulama sonucu bu kimlikle geri döner.
 * {@code cartId} doğrulanacak sepettir. {@code eventType}, aynı topic'te akan istek ve
 * sonuç olaylarını ayırt etmek için taşınır.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCheckoutRequestedPayload(
        String eventType,
        Long orderId,
        Long cartId
) {
}
