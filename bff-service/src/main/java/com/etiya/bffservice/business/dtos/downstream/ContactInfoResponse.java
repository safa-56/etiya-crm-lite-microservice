package com.etiya.bffservice.business.dtos.downstream;

/**
 * customer-service'ten gelen iletişim bilgisi yanıtı (deserileştirme hedefi).
 * Bilinmeyen alanlar Jackson tarafından yok sayılır.
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
