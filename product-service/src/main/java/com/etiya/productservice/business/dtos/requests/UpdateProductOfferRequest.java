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

        @NotBlank(message = "Teklif adı (name) zorunludur.")
        @Size(max = 150, message = "Teklif adı en fazla 150 karakter olabilir.")
        String name,

        @NotNull(message = "Fiyat (price) zorunludur.")
        @DecimalMin(value = "0.0", message = "Fiyat negatif olamaz.")
        BigDecimal price,

        LocalDate startDate,

        LocalDate endDate
) {
}
