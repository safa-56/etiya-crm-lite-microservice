package com.etiya.productservice.business.dtos.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Ürün teklifi güncelleme isteği. Katalog/kampanya bağları bu uçtan değiştirilmez.
 */
public record UpdateProductOfferRequest(

        @NotBlank(message = "{validation.offerName.notBlank}")
        @Size(max = 150, message = "{validation.offerName.size}")
        String name,

        @NotNull(message = "{validation.price.notNull}")
        @DecimalMin(value = "0.0", message = "{validation.price.decimalMin}")
        BigDecimal price,

        LocalDate startDate,

        LocalDate endDate
) {
}
