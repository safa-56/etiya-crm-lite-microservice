package com.etiya.productservice.business.dtos.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ürün yanıtı (satılmış ürün).
 *
 * <p>{@code status}, ürünün {@code general_status} tablosundaki durumunun stabil iş
 * kodudur (shortCode): oluşturma yanıtı {@code PNDG} (Beklemede) döner; saga
 * tamamlanınca ürün {@code ACTV} (onay) ya da {@code QUOTE_DEL} (telafi/iptal) olur.
 */
public record ProductResponse(
        Long id,
        String name,
        Long productOfferId,
        Long accountId,
        Long campaignId,
        Long addressId,
        BigDecimal priceToBePaid,
        String status,
        String statusReason,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
