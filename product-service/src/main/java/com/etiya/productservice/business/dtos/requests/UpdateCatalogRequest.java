package com.etiya.productservice.business.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Katalog güncelleme isteği.
 */
public record UpdateCatalogRequest(

        @NotBlank(message = "{validation.catalogName.notBlank}")
        @Size(max = 150, message = "{validation.catalogName.size}")
        String name,

        @Size(max = 1000, message = "{validation.description.size}")
        String description
) {
}
