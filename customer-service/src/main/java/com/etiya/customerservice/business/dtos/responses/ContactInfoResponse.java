package com.etiya.customerservice.business.dtos.responses;

/**
 * Müşteri iletişim bilgisi yanıtı.
 */
public record ContactInfoResponse(
        Long id,
        String email,
        String homePhone,
        String mobilPhone,
        String fax
) {
}
