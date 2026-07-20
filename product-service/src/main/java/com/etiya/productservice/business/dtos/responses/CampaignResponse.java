package com.etiya.productservice.business.dtos.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Kampanya (paket) yanıtı.
 *
 * <p>Paketin tek satış fiyatı ({@code campaignPrice}), içindeki tekliflerin gerçek
 * liste fiyatları toplamı ({@code listPriceTotal}) ve indirim tutarı
 * ({@code savings = listPriceTotal - campaignPrice}) birlikte döner. {@code offers}
 * paketin içeriğidir; kampanya sepete eklendiğinde bu satırlar kullanılır. Böylece
 * ileride sipariş (order) akışı ek sorgu yapmadan paketi tek yanıttan kurabilir.
 */
public record CampaignResponse(
        Long id,
        String name,
        BigDecimal campaignPrice,
        BigDecimal listPriceTotal,
        BigDecimal savings,
        List<CampaignOfferLine> offers,
        String status,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
