package com.etiya.customerservice.business.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateContactInfoRequest(

        @Email(message = "{validation.email.email}")
        @NotBlank(message = "{validation.email.notBlank}")
        @Size(max = 150)
        String email,

        @Size(max = 20)
        String homePhone,

        @Size(min = 11, max = 15)
        @NotBlank(message = "{validation.mobilePhone.notBlank}")
        String mobilePhone,

        @Size(max = 20)
        String fax
) {
}
