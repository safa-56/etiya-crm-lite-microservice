package com.etiya.bffservice.business.dtos.downstream;

/**
 * customer-service'ten gelen adres yanıtı (deserileştirme hedefi).
 */
public record AddressResponse(
        Long id,
        Long customerId,
        String city,
        String street,
        String houseNumber,
        String addressDescription,
        Boolean isPrimary
) {
}
