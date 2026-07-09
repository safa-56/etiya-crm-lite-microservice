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
        Long productSpecId,
        String productSpecName,
        BigDecimal price,
        LocalDate startDate,
        LocalDate endDate,
        Boolean isActive,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
