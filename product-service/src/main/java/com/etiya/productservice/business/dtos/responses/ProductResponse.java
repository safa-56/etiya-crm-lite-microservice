package com.etiya.productservice.business.dtos.responses;

import com.etiya.productservice.entities.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ürün yanıtı (satılmış ürün).
 *
 * <p>{@code status}, satış saga'sının durumunu taşır: oluşturma yanıtı {@code PENDING}
 * döner; saga tamamlanınca ürün {@code ACTIVE} (onay) ya da {@code CANCELLED} (telafi)
 * olur.
 */
public record ProductResponse(
        Long id,
        String name,
        Long productOfferId,
        Long accountId,
        Long campaignId,
        Long addressId,
        BigDecimal priceToBePaid,
        ProductStatus status,
        String statusReason,
        Boolean isActive,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
