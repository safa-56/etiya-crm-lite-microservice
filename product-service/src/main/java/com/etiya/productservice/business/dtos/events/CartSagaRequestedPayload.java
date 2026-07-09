package com.etiya.productservice.business.dtos.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Sepet ekleme Saga'sı adım 1 gövdesi (cart-service'ten gelen doğrulama isteği).
 *
 * <p>{@code cartItemId} saga korelasyon kimliğidir; sonuç bu kimlikle geri döner.
 * {@code itemType} "OFFER" ise {@code productOfferId}, "CAMPAIGN" ise {@code campaignId}
 * doludur. {@code eventType}, aynı topic'teki sonuç olaylarından ayırt etmek için taşınır.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CartSagaRequestedPayload(
        String eventType,
        Long cartItemId,
        String itemType,
        Long productOfferId,
        Long campaignId
) {
}
