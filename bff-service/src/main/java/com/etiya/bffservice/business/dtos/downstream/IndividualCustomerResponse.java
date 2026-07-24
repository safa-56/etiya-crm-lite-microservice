package com.etiya.bffservice.business.dtos.downstream;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * customer-service {@code GET /api/v1/individual-customers/{id}} yanıtı
 * (deserileştirme hedefi). {@code genderType} enum yerine String olarak alınır;
 * BFF alan kuplajı taşımaz.
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
        String genderType,
        String status,
        LocalDateTime createdDate,
        LocalDateTime updatedDate,
        List<ContactInfoResponse> contactInfos,
        List<AddressResponse> addresses
) {
}
