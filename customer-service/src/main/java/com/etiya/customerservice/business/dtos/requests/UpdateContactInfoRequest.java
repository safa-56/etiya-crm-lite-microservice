package com.etiya.customerservice.business.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Müşteri iletişim bilgisi güncelleme isteği (tek başına
 * {@code CustomerContactInfosController} üzerinden).
 *
 * <p>{@code id}, güncellenecek kaydı belirtir; controller yol değişkeni (path)
 * ile gövdedeki id'yi tutarlı hale getirir. İlişkili müşteri değiştirilmez.
 */
public record UpdateContactInfoRequest(

        Long id,

        @Email(message = "Geçerli bir e-posta adresi giriniz.")
        @Size(max = 150)
        String email,

        @Size(max = 20)
        String homePhone,

        @Size(min = 11, max = 11)
        String mobilPhone,

        @Size(max = 20)
        String fax
) {
}
