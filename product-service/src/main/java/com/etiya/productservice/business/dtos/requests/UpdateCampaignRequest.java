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

        @NotBlank(message = "{validation.campaignName.notBlank}")
        @Size(max = 150, message = "{validation.campaignName.size}")
        String name,

        @NotNull(message = "{validation.campaignPrice.notNull}")
        @DecimalMin(value = "0.0", message = "{validation.campaignPrice.decimalMin}")
        BigDecimal campaignPrice,

        @NotEmpty(message = "{validation.offerIds.notEmpty}")
        List<Long> offerIds
) {
}
