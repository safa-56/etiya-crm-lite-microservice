package com.etiya.cartservice.business.dtos.responses;

import com.etiya.cartservice.entities.enums.CartItemStatus;
import com.etiya.cartservice.entities.enums.CartItemType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sepet satırı yanıtı.
 *
 * <p>Satır bir Saga ile kesinleştiğinden {@code status} (PENDING/ACTIVE/CANCELLED) de
 * döner: yeni eklenen satır kısa süre PENDING görünür, product-service doğrulaması
 * gelince ACTIVE olur (ad/fiyat dolar). Türüne göre alanlar dolar: OFFER satırında
 * {@code productOfferId}; CAMPAIGN satırında {@code campaignId} ve {@code campaignOffers}
 * (paket içeriği snapshot'ı). {@code lineTotal} = {@code unitPrice * quantity} (PENDING
 * iken 0).
 */
public record CartItemResponse(
        Long id,
        CartItemType itemType,
        CartItemStatus status,
        String statusReason,
        Long productOfferId,
        Long campaignId,
        String name,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineTotal,
        List<CampaignOfferLineResponse> campaignOffers
) {
}
