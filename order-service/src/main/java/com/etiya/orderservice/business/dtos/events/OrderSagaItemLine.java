package com.etiya.orderservice.business.dtos.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Saga doğrulama sonucundaki tek bir sepet kalemi (snapshot).
 *
 * <p>cart-service, doğruladığı sepetin aktif satırlarını bu kalemlerle gönderir;
 * order-service bunları sipariş satırlarına ({@code order_items}) snapshot'lar.
 *
 * @param itemType       kalem türü ("OFFER" | "CAMPAIGN")
 * @param productOfferId ürün teklifi kimliği (OFFER kaleminde dolu)
 * @param campaignId     kampanya kimliği (CAMPAIGN kaleminde dolu)
 * @param name           kalem adı
 * @param unitPrice      birim fiyat
 * @param quantity       adet
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderSagaItemLine(
        String itemType,
        Long productOfferId,
        Long campaignId,
        String name,
        BigDecimal unitPrice,
        Integer quantity
) {
}
