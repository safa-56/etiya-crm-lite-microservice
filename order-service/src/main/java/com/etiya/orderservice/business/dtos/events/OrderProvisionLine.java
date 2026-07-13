package com.etiya.orderservice.business.dtos.events;

import java.math.BigDecimal;

/**
 * {@link OrderConfirmedPayload} içinde taşınan tek bir provizyon satırı — kesinleşmiş
 * bir sipariş kaleminin ({@link com.etiya.orderservice.entities.OrderItem}) snapshot'ı.
 *
 * <p>product-service bu satırdan {@code Product} kaydı/kayıtları üretir:
 * <ul>
 *   <li><b>OFFER</b>    : {@code productOfferId} dolu → tek ürün provizyone edilir.</li>
 *   <li><b>CAMPAIGN</b> : {@code campaignId} dolu → product-service kampanyanın paket
 *       içeriğini kendi otoriter DB'sinden çözerek her teklif için bir ürün üretir.</li>
 * </ul>
 *
 * @param itemType       kalem türü ("OFFER" / "CAMPAIGN")
 * @param productOfferId ürün teklifi kimliği (OFFER satırlarında dolu)
 * @param campaignId     kampanya kimliği (CAMPAIGN satırlarında dolu)
 * @param name           kalem adı snapshot'ı
 * @param unitPrice      birim fiyat snapshot'ı (satılan/ödenen fiyat)
 * @param quantity       adet
 */
public record OrderProvisionLine(
        String itemType,
        Long productOfferId,
        Long campaignId,
        String name,
        BigDecimal unitPrice,
        Integer quantity
) {
}
