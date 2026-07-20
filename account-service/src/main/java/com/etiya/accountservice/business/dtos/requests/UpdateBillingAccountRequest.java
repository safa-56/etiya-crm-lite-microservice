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
