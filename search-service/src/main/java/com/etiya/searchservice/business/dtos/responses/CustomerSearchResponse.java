package com.etiya.searchservice.business.dtos.responses;

import com.etiya.searchservice.entities.enums.CustomerRole;

/**
 * Müşteri arama sonucu satırı (FR-002 ACC-21).
 *
 * <p>Sonuç listesinde gösterilen kolonlar: Customer ID, First Name, Second Name,
 * Last Name, Role, ID Number (TCKN).
 */
public record CustomerSearchResponse(
        Long customerId,
        String firstName,
        String secondName,
        String lastName,
        CustomerRole role,
        String nationalityId
) {
}
