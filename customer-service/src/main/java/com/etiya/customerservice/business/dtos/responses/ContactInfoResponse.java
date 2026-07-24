package com.etiya.customerservice.business.dtos.responses;

/**
 * Müşteri iletişim bilgisi yanıtı.
 */
public record ContactInfoResponse(
        Long id,
        Long customerId,
        String email,
        String homePhone,
        String mobilePhone,
        String fax
) {
}
