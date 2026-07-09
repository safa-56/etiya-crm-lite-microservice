package com.etiya.productservice.business.dtos.events;

import java.math.BigDecimal;

/**
 * Sepet ekleme Saga'sının doğrulama sonucundaki tek bir paket teklifi (kampanya içeriği).
 *
 * @param offerId   teklifin kaynak kimliği
 * @param offerName teklif adı
 * @param listPrice teklifin (kampanya dışı) liste fiyatı
 */
public record CartSagaLine(
        Long offerId,
        String offerName,
        BigDecimal listPrice
) {
}
