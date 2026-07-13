package com.etiya.orderservice.business.dtos.events;

import java.util.List;

/**
 * {@code crm.Order.events} kanalına yayınlanan "sipariş kesinleşti" entegrasyon olayı.
 *
 * <p>Sepetten siparişe geçiş Saga'sı siparişi CONFIRMED yaptıktan sonra üretilir ve
 * product-service tarafından tüketilir: her satır için bir {@code Product} provizyone
 * edilir (böylece fatura hesabının aktif ürün sayacı artar ve ürünler hesap detayında
 * görünür). {@code accountId} ürünlerin bağlanacağı fatura hesabı, {@code addressId}
 * servis adresidir. {@code eventType} gövdeye yazılır ki tüketici doğrudan filtreleyebilsin.
 *
 * @param eventType olay tipi ("OrderConfirmed")
 * @param orderId   kesinleşen sipariş kimliği (korelasyon/gözlemlenebilirlik)
 * @param accountId ürünlerin bağlanacağı fatura hesabı kimliği
 * @param addressId servis adresi kimliği (customer-service adres referansı)
 * @param items     provizyone edilecek kalemler
 */
public record OrderConfirmedPayload(
        String eventType,
        Long orderId,
        Long accountId,
        Long addressId,
        List<OrderProvisionLine> items
) {
}
