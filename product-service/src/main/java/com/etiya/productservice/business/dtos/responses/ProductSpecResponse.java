package com.etiya.productservice.business.dtos.responses;

import java.time.LocalDateTime;

/**
 * Ürün teknik özelliği yanıtı.
 */
public record ProductSpecResponse(
        Long id,
        String name,
        String description,
        String status,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
