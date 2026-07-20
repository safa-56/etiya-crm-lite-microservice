package com.etiya.productservice.business.dtos.responses;

import java.time.LocalDateTime;

/**
 * Katalog yanıtı.
 */
public record CatalogResponse(
        Long id,
        String name,
        String description,
        String status,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
