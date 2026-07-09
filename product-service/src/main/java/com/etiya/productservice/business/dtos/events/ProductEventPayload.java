package com.etiya.productservice.business.dtos.events;

/**
 * Outbox'a yazılan ve Debezium ile {@code crm.Product.events} topic'ine yayınlanan
 * ürün olayı gövdesi.
 *
 * <p>account-service bu olayı tüketerek fatura hesabının aktif ürün sayısını
 * ({@code active_product_count}) günceller. Alan adları (özellikle
 * {@code billingAccountId} ve {@code eventType}) account-service'in beklediği
 * sözleşme ile birebir uyumludur; olay tipi gövdeye yazılır ki tüketici doğrudan
 * gövdeden okuyabilsin.
 *
 * @param productId        ürün kimliği
 * @param billingAccountId ürünün bağlı olduğu fatura hesabı kimliği
 * @param eventType        "ProductCreated" / "ProductDeleted"
 */
public record ProductEventPayload(
        Long productId,
        Long billingAccountId,
        String eventType
) {
}
