package com.etiya.productservice.business.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Katalog güncelleme isteği.
 */
public record UpdateCatalogRequest(

        @NotBlank(message = "Katalog adı (name) zorunludur.")
        @Size(max = 150, message = "Katalog adı en fazla 150 karakter olabilir.")
        String name,

        @Size(max = 1000, message = "Açıklama en fazla 1000 karakter olabilir.")
        String description
) {
}
