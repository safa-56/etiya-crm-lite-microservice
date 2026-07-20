package com.etiya.customerservice.business.dtos.requests;

import com.etiya.customerservice.entities.enums.GenderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record UpdateIndividualCustomerRequest(

        @NotBlank(message = "{validation.firstName.notBlank}")
        @Size(max = 50)
        String firstName,

        @Size(max = 100)
        String secondName,

        @NotBlank(message = "{validation.lastName.notBlank}")
        @Size(max = 50)
        String lastName,

        @Past(message = "{validation.birthDate.past}")
        @NotNull(message = "{validation.birthDate.notNull}")
        LocalDate birthDate,

        @Size(max = 100)
        String fatherName,

        @Size(max = 100)
        String motherName,

        @NotBlank(message = "{validation.nationalityId.notBlank}")
        @Pattern(regexp = "\\d{11}", message = "{validation.nationalityId.pattern}")
        String nationalityId,

        @NotNull(message = "{validation.genderType.notNull}")
        GenderType genderType,

        @Valid
        List<CreateContactInfoRequest> contactInfos,

        @Valid
        List<CreateAddressRequest> addresses
) {
}
