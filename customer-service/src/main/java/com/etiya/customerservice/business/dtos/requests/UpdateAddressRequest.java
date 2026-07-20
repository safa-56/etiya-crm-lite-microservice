package com.etiya.customerservice.business.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAddressRequest(

        @Size(max = 100)
        @NotBlank(message = "{validation.city.notBlank}")
        String city,

        @Size(max = 150)
        @NotBlank(message = "{validation.street.notBlank}")
        String street,

        @Size(max = 30)
        @NotBlank(message = "{validation.houseNumber.notBlank}")
        String houseNumber,

        @Size(max = 500)
        @NotBlank(message = "{validation.addressDescription.notBlank}")
        String addressDescription,

        @NotNull
        Boolean isPrimary
) {
}
