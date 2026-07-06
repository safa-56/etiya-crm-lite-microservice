package com.etiya.customerservice.business.dtos.responses;

/**
 * Adres yanıtı.
 */
public record AddressResponse(
        Long id,
        String city,
        String street,
        String houseNumber,
        String addressDescription,
        Boolean isPrimary
) {
}
