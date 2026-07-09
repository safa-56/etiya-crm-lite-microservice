package com.etiya.cartservice.business.dtos.events;

/**
 * Saga adım 1 gövdesi: cart-service, PENDING bir sepet satırı için product-service'ten
 * teklif/kampanya doğrulaması ister.
 *
 * <p>{@code cartItemId} saga korelasyon kimliğidir; doğrulama sonucu bu kimlikle geri
 * döner. {@code itemType} "OFFER" ise {@code productOfferId}, "CAMPAIGN" ise
 * {@code campaignId} dolu olur. {@code eventType}, aynı topic'te akan istek ve sonuç
 * olaylarını ayırt etmek için taşınır.
 */
public record CartItemSagaRequestedPayload(
        String eventType,
        Long cartItemId,
        String itemType,
        Long productOfferId,
        Long campaignId
) {
}
