package com.etiya.productservice.business.dtos.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Kampanya (paket) güncelleme isteği.
 *
 * <p>{@code offerIds} verildiğinde kampanyanın teklif içeriği bu liste ile
 * <b>tamamen değiştirilir</b> (mevcut bağlar pasifleştirilir, yenileri kurulur).
 */
public record UpdateCampaignRequest(

        @NotBlank(message = "Kampanya adı (name) zorunludur.")
        @Size(max = 150, message = "Kampanya adı en fazla 150 karakter olabilir.")
        String name,

        @NotNull(message = "Paket fiyatı (campaignPrice) zorunludur.")
        @DecimalMin(value = "0.0", message = "Paket fiyatı negatif olamaz.")
        BigDecimal campaignPrice,

        @NotEmpty(message = "Kampanya en az bir ürün teklifi (offerIds) içermelidir.")
        List<Long> offerIds
) {
}
