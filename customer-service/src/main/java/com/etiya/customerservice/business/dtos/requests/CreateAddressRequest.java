package com.etiya.customerservice.business.dtos.requests;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Size;

public record CreateAddressRequest(

        /** Adresin ait olduğu müşteri; yalnızca standalone oluşturmada gönderilir. */
        Long customerId,

        @Size(max = 100)
        @NotBlank(message = "Şehir (city) zorunludur.")
        String city,

        @Size(max = 150)
        @NotBlank(message = "Sokak (street) zorunludur.")
        String street,

        @Size(max = 30)
        @NotBlank(message = "Ev no (houseNumber) zorunludur.")
        String houseNumber,

        @Size(max = 500)
        @NotBlank(message = "Adres açıklaması (addressDescription) zorunludur.")
        String addressDescription,

        @NotBlank
        Boolean isPrimary
) {
}
