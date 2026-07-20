package com.etiya.productservice.business.dtos.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Kampanya (paket) oluşturma isteği.
 *
 * <p>Kampanya, birden çok ürün teklifini tek bir paket fiyatıyla ({@code campaignPrice})
 * bir araya getirir. {@code offerIds} paketin içeriğidir (en az bir teklif);
 * kampanya seçildiğinde bu tekliflerin tümü sepete eklenir. Paket fiyatının liste
 * toplamından düşük olması beklenir ancak kural olarak zorlanmaz.
 */
public record CreateCampaignRequest(

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
