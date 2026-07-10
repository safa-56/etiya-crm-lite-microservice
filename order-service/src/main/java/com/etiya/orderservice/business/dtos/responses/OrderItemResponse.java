package com.etiya.orderservice.business.dtos.responses;

import com.etiya.orderservice.entities.enums.OrderItemType;

import java.math.BigDecimal;

/**
 * Sipariş satırı yanıtı (Order Items — FR-016 ACC-03).
 *
 * <p>Sipariş anındaki sepet kaleminin snapshot'ını döner. Türüne göre alanlar dolar:
 * OFFER satırında {@code productOfferId}; CAMPAIGN satırında {@code campaignId}.
 * {@code lineTotal} = {@code unitPrice * quantity}.
 */
public record OrderItemResponse(
        Long id,
        OrderItemType itemType,
        Long productOfferId,
        Long campaignId,
        String name,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineTotal
) {
}
