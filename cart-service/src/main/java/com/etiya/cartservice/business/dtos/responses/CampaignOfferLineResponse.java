package com.etiya.cartservice.business.dtos.responses;

import java.math.BigDecimal;

/**
 * Sepetteki bir kampanya satırının içindeki tek teklif (paket içeriği gösterimi).
 *
 * <p>CAMPAIGN türündeki sepet satırları, tek paket fiyatıyla eklenir; kullanıcıya
 * paketin hangi tekliflerden oluştuğunu göstermek için bu satırlar yerel kampanya
 * projeksiyonundan doldurulur.
 */
public record CampaignOfferLineResponse(
        Long offerId,
        String offerName,
        BigDecimal listPrice
) {
}
