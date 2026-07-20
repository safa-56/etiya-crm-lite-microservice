package com.etiya.productservice.business.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Ürün teknik özelliği oluşturma isteği.
 */
public record CreateProductSpecRequest(

        @NotBlank(message = "{validation.specName.notBlank}")
        @Size(max = 150, message = "{validation.specName.size}")
        String name,

        @Size(max = 1000, message = "{validation.description.size}")
        String description
) {
}
