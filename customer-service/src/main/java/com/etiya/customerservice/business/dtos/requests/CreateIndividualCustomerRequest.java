package com.etiya.customerservice.business.dtos.requests;

import static com.etiya.customerservice.business.constants.ValidationPatterns.NAME_PATTERN;

import com.etiya.customerservice.entities.enums.GenderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Bireysel müşteri oluşturma isteği.
 */
public record CreateIndividualCustomerRequest(

        @NotBlank(message = "{validation.firstName.notBlank}")
        @Size(max = 50)
        @Pattern(regexp = NAME_PATTERN, message = "{validation.name.pattern}")
        String firstName,

        @Size(max = 100)
        @Pattern(regexp = NAME_PATTERN, message = "{validation.name.pattern}")
        String secondName,

        @NotBlank(message = "{validation.lastName.notBlank}")
        @Size(max = 50)
        @Pattern(regexp = NAME_PATTERN, message = "{validation.name.pattern}")
        String lastName,

        @Past(message = "{validation.birthDate.past}")
        @NotNull(message = "{validation.birthDate.notNull}")
        LocalDate birthDate,

        @Size(max = 100)
        @Pattern(regexp = NAME_PATTERN, message = "{validation.name.pattern}")
        String fatherName,

        @Size(max = 100)
        @Pattern(regexp = NAME_PATTERN, message = "{validation.name.pattern}")
        String motherName,

        @NotBlank(message = "{validation.nationalityId.notBlank}")
        @Pattern(regexp = "\\d{11}", message = "{validation.nationalityId.pattern}")
        String nationalityId,

        @NotNull(message = "{validation.genderType.notNull}")
        GenderType genderType,

        @Valid
        @NotNull
        CreateContactInfoRequest contactInfo,

        @Valid
        @NotNull
        CreateAddressRequest address
) {
}
