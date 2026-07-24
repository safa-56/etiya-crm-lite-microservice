package com.etiya.accountservice.business.dtos.responses;

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
        String status,
        String accountNumber,
        String accountName,
        AccountType accountType,
        String accountDescription,
        Long addressId,
        Long pendingAddressId,
        String address,
        String orderNumber,
        /** Hesaba bağlı aktif ürün sayısı; müşteri/hesap silme kuralında (aktif ürünlü silinemez) kullanılır. */
        Integer activeProductCount,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
