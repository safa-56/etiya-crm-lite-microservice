package com.etiya.accountservice.business.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Fatura hesabı güncelleme isteği (Fatura Hesabını Düzenle ekranı).
 *
 * <p>Yalnızca {@code accountName}, {@code accountDescription} ve {@code addressId}
 * düzenlenir. Account Type/Status ile {@code accountNumber}/{@code orderNumber}
 * sistem-yönetimlidir; oluşturmada üretilir ve güncellemede korunur (istemciden alınmaz).
 */
public record UpdateBillingAccountRequest(

        @NotBlank(message = "{validation.accountName.notBlank}")
        @Size(max = 150, message = "{validation.accountName.size}")
        String accountName,

        @Size(max = 500, message = "{validation.accountDescription.size}")
        String accountDescription,

        @NotNull(message = "{validation.addressId.notNull}")
        Long addressId
) {
}
