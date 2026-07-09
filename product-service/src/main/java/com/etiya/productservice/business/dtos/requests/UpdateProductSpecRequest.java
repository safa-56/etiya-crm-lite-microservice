package com.etiya.productservice.business.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Ürün teknik özelliği güncelleme isteği.
 */
public record UpdateProductSpecRequest(

        @NotBlank(message = "Özellik adı (name) zorunludur.")
        @Size(max = 150, message = "Özellik adı en fazla 150 karakter olabilir.")
        String name,

        @Size(max = 1000, message = "Açıklama en fazla 1000 karakter olabilir.")
        String description
) {
}
