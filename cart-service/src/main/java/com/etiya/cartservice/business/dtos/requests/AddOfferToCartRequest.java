package com.etiya.cartservice.business.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Sepete katalogdan doğrudan bir ürün teklifi ekleme isteği (FR-014, 1. yol).
 *
 * <p>{@code productOfferId}, cart-service'in yerel teklif projeksiyonundan doğrulanır
 * (varlık + aktiflik) ve fiyatı oradan snapshot'lanır. {@code quantity} verilmezse
 * 1 kabul edilir; aynı teklif sepette zaten varsa adedi artırılır.
 */
public record AddOfferToCartRequest(

        @NotNull(message = "Ürün teklifi (productOfferId) zorunludur.")
        Long productOfferId,

        @Min(value = 1, message = "Adet en az 1 olmalıdır.")
        Integer quantity
) {

    /** Adet verilmediyse varsayılan olarak 1 döndürür. */
    public int quantityOrDefault() {
        return quantity == null ? 1 : quantity;
    }
}
