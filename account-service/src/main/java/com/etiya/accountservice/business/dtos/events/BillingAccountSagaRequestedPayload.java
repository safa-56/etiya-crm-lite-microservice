package com.etiya.accountservice.business.dtos.events;

/**
 * Saga adım 1 gövdesi: account-service, PENDING bir fatura hesabı için
 * customer-service'ten müşteri/adres doğrulaması ister.
 *
 * <p>{@code billingAccountId} saga korelasyon kimliğidir; doğrulama sonucu bu
 * kimlikle geri döner. {@code eventType}, tüketicinin (aynı topic'te akan) istek ve
 * sonuç olaylarını payload'dan ayırt edebilmesi için taşınır.
 */
public record BillingAccountSagaRequestedPayload(
        String eventType,
        Long billingAccountId,
        Long customerId,
        Long addressId
) {
}
