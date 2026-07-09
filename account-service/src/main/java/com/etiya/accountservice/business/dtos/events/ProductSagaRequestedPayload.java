package com.etiya.accountservice.business.dtos.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Ürün satışı Saga'sının istek gövdesi (product-service -> account-service).
 *
 * <p>product-service, PENDING bir ürün için fatura hesabı doğrulaması ister.
 * {@code productId} saga korelasyon kimliğidir; sonuç bu kimlikle geri döner.
 * Bilinmeyen alanlar yok sayılır.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductSagaRequestedPayload(
        String eventType,
        Long productId,
        Long billingAccountId
) {
}
