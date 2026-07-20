package com.etiya.productservice.business.dtos.responses;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Ürün teklifi yanıtı. Teklif Seçimi ekranında (FR-014) Prod Offer ID / Name ve
 * fiyat gösterimi bu yanıttan beslenir.
 */
public record ProductOfferResponse(
        Long id,
        String name,
        Long catalogId,
        String catalogName,
        Long productSpecId,
        String productSpecName,
        BigDecimal price,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
