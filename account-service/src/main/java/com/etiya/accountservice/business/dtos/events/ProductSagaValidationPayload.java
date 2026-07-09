package com.etiya.accountservice.business.dtos.events;

/**
 * Ürün satışı Saga'sının sonuç gövdesi (account-service -> product-service).
 *
 * <p>{@code valid=true} ise product-service ürünü ACTIVE yapar; {@code valid=false}
 * ise {@code reason} doldurulur ve product-service telafi ile ürünü CANCELLED yapar.
 * {@code eventType}, aynı topic'teki istek olayından ayırt etmek için taşınır.
 */
public record ProductSagaValidationPayload(
        String eventType,
        Long productId,
        Long billingAccountId,
        boolean valid,
        String reason
) {
}
