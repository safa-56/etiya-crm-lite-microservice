package com.etiya.customerservice.business.dtos.requests;

import com.etiya.customerservice.entities.enums.GenderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * Bireysel müşteri oluşturma isteği.
 */
public record CreateIndividualCustomerRequest(

        @NotBlank(message = "İsim (first_name) zorunludur.")
        @Size(max = 50)
        String firstName,

        @Size(max = 100)
        String secondName,

        @NotBlank(message = "Soyisim (last_name) zorunludur.")
        @Size(max = 50)
        String lastName,

        @Past(message = "Doğum tarihi geçmiş bir tarih olmalıdır.")
        @NotBlank(message = "Doğum tarihi (birthDate) zorunludur.")
        LocalDate birthDate,

        @Size(max = 100)
        String fatherName,

        @Size(max = 100)
        String motherName,

        @NotBlank(message = "TC kimlik numarası (nationality_id) zorunludur.")
        @Min(value = 10_000_000_000L, message = "TC kimlik numarası 11 haneli olmalıdır.")
        @Max(value = 99_999_999_999L, message = "TC kimlik numarası 11 haneli olmalıdır.")
        Long nationalityId,

        @NotBlank(message = "Cinsiyet (genderType) zorunludur.")
        GenderType genderType,

        @Valid
        List<CreateContactInfoRequest> contactInfos,

        @Valid
        List<CreateAddressRequest> addresses
) {
}
