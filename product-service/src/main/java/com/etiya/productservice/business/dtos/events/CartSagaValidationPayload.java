package com.etiya.productservice.business.dtos.events;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sepet ekleme Saga'sı adım 2 gövdesi (product-service'in doğrulama sonucu).
 *
 * <p>{@code valid=true} ise cart-service satırı ACTIVE yapar ve {@code name}/{@code
 * unitPrice} snapshot'ını (kampanyada {@code offers} paket içeriğini) yazar;
 * {@code valid=false} ise {@code reason} doldurulur ve cart-service telafi ile satırı
 * CANCELLED yapar. {@code eventType}, aynı topic'teki istek olayından ayırt etmek için
 * taşınır. Alan adları cart-service sözleşmesiyle birebir uyumludur.
 */
public record CartSagaValidationPayload(
        String eventType,
        Long cartItemId,
        boolean valid,
        String reason,
        String name,
        BigDecimal unitPrice,
        List<CartSagaLine> offers
) {
}
