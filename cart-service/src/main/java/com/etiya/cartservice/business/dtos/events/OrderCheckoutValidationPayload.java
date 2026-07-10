package com.etiya.cartservice.business.dtos.events;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sepetten siparişe geçiş Saga'sı adım 2 gövdesi (cart-service doğrulayıcı tarafında
 * <b>üretilir</b>): sepet doğrulama sonucu.
 *
 * <p>{@code valid=true} ise order-service siparişi CONFIRMED yapar; sepet sahipliğini
 * ({@code customerId}/{@code accountId}), {@code totalAmount} toplamını ve {@code items}
 * kalemlerini sipariş satırlarına snapshot'lar. {@code valid=false} ise {@code reason}
 * doldurulur ve order-service telafi ile siparişi CANCELLED yapar. {@code eventType},
 * aynı topic'teki istek olayından ayırt etmek için taşınır.
 */
public record OrderCheckoutValidationPayload(
        String eventType,
        Long orderId,
        Long cartId,
        boolean valid,
        String reason,
        Long customerId,
        Long accountId,
        BigDecimal totalAmount,
        List<OrderSagaItemLine> items
) {
}
