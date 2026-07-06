package com.etiya.customerservice.business.dtos.requests;

import jakarta.validation.constraints.Size;

/**
 * Adres oluşturma isteği (müşteri isteğinin içinde iç içe kullanılır).
 */
public record CreateAddressRequest(

        @Size(max = 100)
        String city,

        @Size(max = 150)
        String street,

        @Size(max = 30)
        String houseNumber,

        @Size(max = 500)
        String addressDescription,

        Boolean isPrimary
) {
}
