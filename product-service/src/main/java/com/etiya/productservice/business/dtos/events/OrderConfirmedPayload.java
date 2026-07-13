package com.etiya.productservice.business.dtos.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * order-service'in {@code crm.Order.events} kanalına yayınladığı "sipariş kesinleşti"
 * olayının tüketici tarafı gövdesi.
 *
 * <p>product-service bu olayı tüketip her satır için {@code Product} provizyone eder;
 * böylece fatura hesabının ({@code accountId}) aktif ürün sayacı artar ve ürünler hesap
 * detayında görünür. Alan adları order-service sözleşmesiyle birebir uyumludur.
 *
 * @param eventType olay tipi ("OrderConfirmed")
 * @param orderId   kesinleşen sipariş kimliği (korelasyon/gözlemlenebilirlik)
 * @param accountId ürünlerin bağlanacağı fatura hesabı kimliği
 * @param addressId servis adresi kimliği (customer-service adres referansı)
 * @param items     provizyone edilecek kalemler
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderConfirmedPayload(
        String eventType,
        Long orderId,
        Long accountId,
        Long addressId,
        List<OrderProvisionLine> items
) {
}
