package com.etiya.productservice.business.dtos.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Kampanya güncelleme isteği.
 */
public record UpdateCampaignRequest(

        @NotBlank(message = "Kampanya adı (name) zorunludur.")
        @Size(max = 150, message = "Kampanya adı en fazla 150 karakter olabilir.")
        String name,

        @DecimalMin(value = "0.0", message = "Toplam fiyat negatif olamaz.")
        BigDecimal totalPrice
) {
}
