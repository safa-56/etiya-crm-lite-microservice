package com.etiya.accountservice.business.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Fatura hesabı oluşturma isteği.
 *
 * <p>Create Billing Account ekranının zorunlu/opsiyonel alanlarını taşır:
 * {@code accountName} (zorunlu), {@code accountDescription} (opsiyonel),
 * {@code addressId} (zorunlu). Hesap bir müşteriye bağlı olduğundan
 * {@code customerId} de zorunludur.
 *
 * <p>{@code addressId}, müşterinin customer-service'teki adreslerinden birinin
 * kimliğidir (FR-007 adres seçme davranışı). Bu adresin gerçekten o müşteriye ait
 * olduğu, account-service'in Kafka ile beslenen yerel müşteri projeksiyonundan
 * doğrulanır ve adres metni oradan çözülür.
 *
 * <p>{@code accountNumber} (alfanümerik, ≤30) ve {@code orderNumber}
 * (alfanümerik, ≤20) opsiyoneldir; verilirse kabul kriterlerindeki kısıtlara
 * uymalıdır. {@code accountType} ve {@code accountStatus} istemciden alınmaz;
 * sistem tarafından atanır (Billing Account / Active).
 */
public record CreateBillingAccountRequest(

        @NotNull(message = "{validation.customerId.notNull}")
        Long customerId,

        @NotBlank(message = "{validation.accountName.notBlank}")
        @Size(max = 150, message = "{validation.accountName.size}")
        String accountName,

        @Size(max = 500, message = "{validation.accountDescription.size}")
        String accountDescription,

        @NotNull(message = "{validation.addressId.notNull}")
        Long addressId,

        @Size(max = 30, message = "{validation.accountNumber.size}")
        @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "{validation.accountNumber.pattern}")
        String accountNumber,

        @Size(max = 20, message = "{validation.orderNumber.size}")
        @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "{validation.orderNumber.pattern}")
        String orderNumber
) {
}
