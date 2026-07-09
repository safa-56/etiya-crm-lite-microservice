package com.etiya.cartservice.business.dtos.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * Saga adım 2 gövdesi: product-service'in teklif/kampanya doğrulama sonucu.
 *
 * <p>{@code valid=true} ise cart-service satırı ACTIVE yapar ve {@code name}/{@code
 * unitPrice} snapshot'ını (kampanyada {@code offers} paket içeriğini) yazar;
 * {@code valid=false} ise {@code reason} doldurulur ve cart-service telafi ile satırı
 * CANCELLED yapar. {@code eventType}, aynı topic'teki istek olayından ayırt etmek için
 * taşınır.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CartItemSagaValidationPayload(
        String eventType,
        Long cartItemId,
        boolean valid,
        String reason,
        String name,
        BigDecimal unitPrice,
        List<CartItemSagaLine> offers
) {
}
