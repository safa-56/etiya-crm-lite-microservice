package com.etiya.customerservice.business.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Müşteri iletişim bilgisi oluşturma isteği (müşteri isteğinin içinde iç içe kullanılır).
 */
public record CreateContactInfoRequest(

        @Email(message = "Geçerli bir e-posta adresi giriniz.")
        @Size(max = 150)
        String email,

        @Size(max = 20)
        String homePhone,

        @Size(max = 20)
        String mobilPhone,

        @Size(max = 20)
        String fax
) {
}
