package com.etiya.customerservice.business.dtos.events;

/**
 * Saga adım 2 gövdesi (yayınlanır): customer-service'in müşteri/adres doğrulama
 * sonucu. account-service sözleşmesiyle birebir eşleşir.
 *
 * <p>{@code valid=true} ise otoriter adres alanları doldurulur; {@code valid=false}
 * ise {@code reason} doldurulur (adres alanları null).
 */
public record BillingAccountSagaValidationPayload(
        String eventType,
        Long billingAccountId,
        Long customerId,
        Long addressId,
        boolean valid,
        String reason,
        String city,
        String street,
        String houseNumber,
        String addressDescription
) {
}
