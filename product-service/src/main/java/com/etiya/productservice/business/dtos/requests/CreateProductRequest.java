package com.etiya.productservice.business.dtos.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Ürün (satış) oluşturma isteği.
 *
 * <p>Bir ürün teklifinin ({@code productOfferId}) bir fatura hesabına
 * ({@code accountId}) satılmış hali olan {@link com.etiya.productservice.entities.Product}
 * kaydını üretir. {@code accountId} account-service'teki fatura hesabına,
 * {@code addressId} customer-service'teki servis adresine referanstır (yerel FK
 * değil). Ürün oluşturulduğunda outbox üzerinden {@code ProductCreated} olayı
 * yayınlanır ve account-service aktif ürün sayısını günceller.
 */
public record CreateProductRequest(

        @NotNull(message = "{validation.productOfferId.notNull}")
        Long productOfferId,

        @NotNull(message = "{validation.accountId.notNull}")
        Long accountId,

        @Size(max = 150, message = "{validation.productName.size}")
        String name,

        /** Opsiyonel: ürünün ait olduğu kampanya. */
        Long campaignId,

        /** Opsiyonel: servis adresi (customer-service adres id'si). */
        Long addressId,

        @NotNull(message = "{validation.priceToBePaid.notNull}")
        @DecimalMin(value = "0.0", message = "{validation.priceToBePaid.decimalMin}")
        BigDecimal priceToBePaid
) {
}
