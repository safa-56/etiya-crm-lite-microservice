package com.etiya.customerservice.business.dtos.responses;

import com.etiya.customerservice.entities.enums.GenderType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Bireysel müşteri yanıtı (GET/CREATE/UPDATE dönüşleri için ortak).
 */
public record IndividualCustomerResponse(
        Long id,
        String firstName,
        String secondName,
        String lastName,
        LocalDate birthDate,
        String fatherName,
        String motherName,
        Long genderId,
        String nationalityId,
        GenderType genderType,
        Boolean isActive,
        LocalDateTime createdDate,
        LocalDateTime updatedDate,
        List<ContactInfoResponse> contactInfos,
        List<AddressResponse> addresses
) {
}
