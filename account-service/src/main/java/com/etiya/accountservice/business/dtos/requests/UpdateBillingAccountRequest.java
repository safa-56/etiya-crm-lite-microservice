package com.etiya.accountservice.business.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Fatura hesabı güncelleme isteği.
 *
 * <p>{@code id} yol değişkeninden (path) doldurulur; controller gövdeyle
 * tutarlı hale getirir. Account Type/Status güncellenmez (sistem yönetir).
 */
public record UpdateBillingAccountRequest(

        @NotNull(message = "Hesap kimliği (id) zorunludur.")
        Long id,

        @NotBlank(message = "Hesap adı (accountName) zorunludur.")
        @Size(max = 150, message = "Hesap adı en fazla 150 karakter olabilir.")
        String accountName,

        @Size(max = 500, message = "Hesap açıklaması en fazla 500 karakter olabilir.")
        String accountDescription,

        @NotBlank(message = "Adres (address) zorunludur.")
        @Size(max = 500, message = "Adres en fazla 500 karakter olabilir.")
        String address,

        @Size(max = 30, message = "Hesap numarası en fazla 30 karakter olabilir.")
        @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Hesap numarası yalnızca alfanümerik karakter içerebilir.")
        String accountNumber,

        @Size(max = 20, message = "Sipariş numarası en fazla 20 karakter olabilir.")
        @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Sipariş numarası yalnızca alfanümerik karakter içerebilir.")
        String orderNumber
) {
}
