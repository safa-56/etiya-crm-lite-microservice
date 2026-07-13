package com.etiya.productservice.business.dtos.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * {@link OrderConfirmedPayload} içinde taşınan tek bir provizyon satırı — order-service'ten
 * gelen kesinleşmiş sipariş kaleminin snapshot'ı.
 *
 * <p>Alan adları order-service'in yayınladığı sözleşme ({@code OrderProvisionLine}) ile
 * birebir uyumludur (per-service DB gereği paylaşılan modül yoktur, sözleşme kopyalanır).
 *
 * @param itemType       kalem türü ("OFFER" / "CAMPAIGN")
 * @param productOfferId ürün teklifi kimliği (OFFER satırlarında dolu)
 * @param campaignId     kampanya kimliği (CAMPAIGN satırlarında dolu)
 * @param name           kalem adı snapshot'ı
 * @param unitPrice      birim fiyat snapshot'ı (satılan/ödenen fiyat)
 * @param quantity       adet
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderProvisionLine(
        String itemType,
        Long productOfferId,
        Long campaignId,
        String name,
        BigDecimal unitPrice,
        Integer quantity
) {
}
