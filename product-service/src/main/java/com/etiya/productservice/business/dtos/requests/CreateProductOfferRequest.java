package com.etiya.productservice.business.dtos.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Ürün teklifi oluşturma isteği.
 *
 * <p>Bir teknik özelliği ({@code productSpecId}) fiyatlandırır ve <b>zorunlu
 * olarak</b> bir kataloga (kategori — {@code catalogId}) bağlanır. Kampanya
 * üyeliği burada değil, kampanya tarafında ({@code POST /campaigns} — offerIds)
 * yönetilir.
 */
public record CreateProductOfferRequest(

        @NotBlank(message = "{validation.offerName.notBlank}")
        @Size(max = 150, message = "{validation.offerName.size}")
        String name,

        @NotNull(message = "{validation.catalogId.notNull}")
        Long catalogId,

        @NotNull(message = "{validation.productSpecId.notNull}")
        Long productSpecId,

        @NotNull(message = "{validation.price.notNull}")
        @DecimalMin(value = "0.0", message = "{validation.price.decimalMin}")
        BigDecimal price,

        LocalDate startDate,

        LocalDate endDate
) {
}
