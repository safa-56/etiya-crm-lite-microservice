package com.etiya.bffservice.business.dtos.downstream;

import java.time.LocalDateTime;

/**
 * account-service {@code GET /api/v1/billing-accounts/by-customer/{customerId}}
 * yanıt satırı (deserileştirme hedefi). {@code accountType} String olarak alınır.
 */
public record BillingAccountResponse(
        Long id,
        Long customerId,
        String status,
        String accountNumber,
        String accountName,
        String accountType,
        String accountDescription,
        Long addressId,
        Long pendingAddressId,
        String address,
        String orderNumber,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
