package com.etiya.productservice.business.dtos.events;

/**
 * Saga adım 1 gövdesi: product-service, PENDING bir ürün için account-service'ten
 * fatura hesabı doğrulaması ister.
 *
 * <p>{@code productId} saga korelasyon kimliğidir; doğrulama sonucu bu kimlikle
 * geri döner. {@code eventType}, tüketicinin (aynı topic'te akan) istek ve sonuç
 * olaylarını payload'dan ayırt edebilmesi için taşınır.
 */
public record ProductSagaRequestedPayload(
        String eventType,
        Long productId,
        Long billingAccountId
) {
}
