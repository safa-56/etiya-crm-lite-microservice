package com.etiya.productservice.business.dtos.responses;

import java.math.BigDecimal;

/**
 * Kampanya paketindeki tek bir teklif satırı.
 *
 * <p>Teklif Seçimi ekranının Campaign sekmesi sonuç tablosunu (Campaign ID,
 * Prod Offer ID, Prod Offer Name) ve sepet satırlarını besler.
 */
public record CampaignOfferLine(
        Long offerId,
        String offerName,
        BigDecimal listPrice
) {
}
