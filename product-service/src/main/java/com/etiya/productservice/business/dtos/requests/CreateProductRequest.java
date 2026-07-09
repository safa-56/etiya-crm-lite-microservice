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

        @NotNull(message = "Ürün teklifi (productOfferId) zorunludur.")
        Long productOfferId,

        @NotNull(message = "Fatura hesabı (accountId) zorunludur.")
        Long accountId,

        @Size(max = 150, message = "Ürün adı en fazla 150 karakter olabilir.")
        String name,

        /** Opsiyonel: ürünün ait olduğu kampanya. */
        Long campaignId,

        /** Opsiyonel: servis adresi (customer-service adres id'si). */
        Long addressId,

        @NotNull(message = "Satış fiyatı (priceToBePaid) zorunludur.")
        @DecimalMin(value = "0.0", message = "Satış fiyatı negatif olamaz.")
        BigDecimal priceToBePaid
) {
}
