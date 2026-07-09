package com.etiya.cartservice.business.dtos.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Saga doğrulama sonucundaki tek bir paket teklifi (kampanya içeriği).
 *
 * <p>product-service, doğruladığı kampanyanın içeriğini bu satırlarla gönderir;
 * cart-service bunları sepet satırının snapshot içeriğine yazar.
 *
 * @param offerId   teklifin kaynak kimliği
 * @param offerName teklif adı
 * @param listPrice teklifin (kampanya dışı) liste fiyatı
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CartItemSagaLine(
        Long offerId,
        String offerName,
        BigDecimal listPrice
) {
}
