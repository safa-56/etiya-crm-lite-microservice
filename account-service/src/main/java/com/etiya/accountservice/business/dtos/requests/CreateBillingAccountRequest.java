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

        @NotNull(message = "Müşteri (customerId) zorunludur.")
        Long customerId,

        @NotBlank(message = "Hesap adı (accountName) zorunludur.")
        @Size(max = 150, message = "Hesap adı en fazla 150 karakter olabilir.")
        String accountName,

        @Size(max = 500, message = "Hesap açıklaması en fazla 500 karakter olabilir.")
        String accountDescription,

        @NotNull(message = "Adres (addressId) zorunludur.")
        Long addressId,

        @Size(max = 30, message = "Hesap numarası en fazla 30 karakter olabilir.")
        @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Hesap numarası yalnızca alfanümerik karakter içerebilir.")
        String accountNumber,

        @Size(max = 20, message = "Sipariş numarası en fazla 20 karakter olabilir.")
        @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Sipariş numarası yalnızca alfanümerik karakter içerebilir.")
        String orderNumber
) {
}
