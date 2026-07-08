package com.etiya.accountservice.business.dtos.events;

/**
 * Saga adım 2 gövdesi: customer-service'in müşteri/adres doğrulama sonucu.
 *
 * <p>{@code valid=true} ise adres alanları (otoriter snapshot) doldurulur;
 * account-service bunları hesaba yazıp durumu ACTIVE yapar. {@code valid=false}
 * ise {@code reason} doldurulur ve account-service telafi ile hesabı CANCELLED yapar.
 * {@code eventType}, aynı topic'teki istek olayından ayırt etmek için taşınır.
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
