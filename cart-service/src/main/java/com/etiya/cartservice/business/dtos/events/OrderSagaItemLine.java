package com.etiya.cartservice.business.dtos.events;

import java.math.BigDecimal;

/**
 * Sepetten siparişe geçiş Saga'sının doğrulama sonucundaki tek bir sepet kalemi (snapshot).
 *
 * <p>cart-service (doğrulayıcı), doğruladığı sepetin aktif satırlarını bu kalemlerle
 * gönderir; order-service bunları sipariş satırlarına ({@code order_items}) snapshot'lar.
 *
 * @param itemType       kalem türü ("OFFER" | "CAMPAIGN")
 * @param productOfferId ürün teklifi kimliği (OFFER kaleminde dolu)
 * @param campaignId     kampanya kimliği (CAMPAIGN kaleminde dolu)
 * @param name           kalem adı
 * @param unitPrice      birim fiyat
 * @param quantity       adet
 */
public record OrderSagaItemLine(
        String itemType,
        Long productOfferId,
        Long campaignId,
        String name,
        BigDecimal unitPrice,
        Integer quantity
) {
}
