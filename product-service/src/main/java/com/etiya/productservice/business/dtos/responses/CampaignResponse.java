package com.etiya.productservice.business.dtos.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Kampanya yanıtı.
 */
public record CampaignResponse(
        Long id,
        String name,
        BigDecimal totalPrice,
        Boolean isActive,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
