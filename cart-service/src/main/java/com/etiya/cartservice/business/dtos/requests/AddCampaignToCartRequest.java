package com.etiya.cartservice.business.dtos.requests;

import jakarta.validation.constraints.NotNull;

/**
 * Sepete bir kampanyayı (paket) ekleme isteği (FR-014, 2. yol).
 *
 * <p>Kampanya, içindeki birden çok teklifle birlikte tek paket fiyatıyla ({@code
 * campaignPrice}) sepete <b>bir bütün olarak</b> eklenir. {@code campaignId},
 * cart-service'in yerel kampanya projeksiyonundan doğrulanır (varlık + aktiflik) ve
 * paket fiyatı oradan snapshot'lanır. Aynı kampanya sepete birden çok kez eklenemez.
 */
public record AddCampaignToCartRequest(

        @NotNull(message = "Kampanya (campaignId) zorunludur.")
        Long campaignId
) {
}
