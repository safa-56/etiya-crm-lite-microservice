package com.etiya.accountservice.business.dtos.responses;

import com.etiya.accountservice.entities.enums.AccountStatus;
import com.etiya.accountservice.entities.enums.AccountType;

import java.time.LocalDateTime;

/**
 * Fatura hesabı yanıtı (GET/CREATE/UPDATE dönüşleri için ortak).
 *
 * <p>Fatura hesapları tablosunun sütunları ({@code accountStatus},
 * {@code accountNumber}, {@code accountName}, {@code accountType}) bu yanıttan
 * beslenir.
 */
public record BillingAccountResponse(
        Long id,
        Long customerId,
        AccountStatus accountStatus,
        String accountNumber,
        String accountName,
        AccountType accountType,
        String accountDescription,
        Long addressId,
        String address,
        String orderNumber,
        Boolean isActive,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
