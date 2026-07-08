package com.etiya.accountservice.business.dtos.events;

/**
 * product-service'ten tüketilen ürün olayı gövdesi.
 *
 * <p>product-service henüz yazılmadığından bu, hesaba bağlı aktif ürün sayısını
 * ({@code active_product_count}) yerel projeksiyonda güncel tutmak için
 * beklenen sözleşmedir. {@code eventType} alanı ürünün eklenip
 * (aktifleşip) mi yoksa kaldırılıp (pasifleşip) mi geldiğini belirtir.
 *
 * @param productId        ürün kimliği
 * @param billingAccountId ürünün bağlı olduğu fatura hesabı kimliği
 * @param eventType        "ProductCreated" / "ProductActivated" / "ProductDeleted" ...
 */
public record ProductEventPayload(
        Long productId,
        Long billingAccountId,
        String eventType
) {
}
