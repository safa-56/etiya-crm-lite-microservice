package com.etiya.customerservice.business.dtos.requests;

import com.etiya.customerservice.entities.enums.GenderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record UpdateIndividualCustomerRequest(

        @NotBlank(message = "İsim (first_name) zorunludur.")
        @Size(max = 50)
        String firstName,

        @Size(max = 100)
        String secondName,

        @NotBlank(message = "Soyisim (last_name) zorunludur.")
        @Size(max = 50)
        String lastName,

        @Past(message = "Doğum tarihi geçmiş bir tarih olmalıdır.")
        @NotNull(message = "Doğum tarihi (birthDate) zorunludur.")
        LocalDate birthDate,

        @Size(max = 100)
        String fatherName,

        @Size(max = 100)
        String motherName,

        @NotBlank(message = "TC kimlik numarası (nationality_id) zorunludur.")
        @Pattern(regexp = "\\d{11}", message = "TC kimlik numarası 11 haneli olmalıdır.")
        String nationalityId,

        @NotNull(message = "Cinsiyet (genderType) zorunludur.")
        GenderType genderType,

        @Valid
        List<CreateContactInfoRequest> contactInfos,

        @Valid
        List<CreateAddressRequest> addresses
) {
}
