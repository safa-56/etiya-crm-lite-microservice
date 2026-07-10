package com.etiya.orderservice.business.dtos.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * Saga adım 2 gövdesi: cart-service'in sepet doğrulama sonucu.
 *
 * <p>{@code valid=true} ise order-service siparişi CONFIRMED yapar; sepet sahipliğini
 * ({@code customerId}/{@code accountId}), {@code totalAmount} toplamını ve {@code items}
 * kalemlerini sipariş satırlarına snapshot'lar. {@code valid=false} ise {@code reason}
 * doldurulur ve order-service telafi ile siparişi CANCELLED yapar. {@code eventType},
 * aynı topic'teki istek olayından ayırt etmek için taşınır.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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
