package com.etiya.customerservice.business.dtos.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Saga adım 1 gövdesi (tüketilir): account-service'in PENDING fatura hesabı için
 * gönderdiği müşteri/adres doğrulama isteği. account-service sözleşmesiyle eşleşir.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BillingAccountSagaRequestedPayload(
        String eventType,
        Long billingAccountId,
        Long customerId,
        Long addressId
) {
}
