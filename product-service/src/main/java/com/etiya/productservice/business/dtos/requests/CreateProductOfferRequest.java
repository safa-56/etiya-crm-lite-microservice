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
 * <p>Bir teknik özelliği ({@code productSpecId}) fiyatlandırır. Teklif isteğe
 * bağlı olarak bir kataloga ({@code catalogId}) ve/veya bir kampanyaya
 * ({@code campaignId}) bağlanabilir; verilirse ilgili birleşim (join) kayıtları
 * oluşturulur (Teklif Seçimi ekranındaki Catalog/Campaign sekmeleri için).
 */
public record CreateProductOfferRequest(

        @NotBlank(message = "Teklif adı (name) zorunludur.")
        @Size(max = 150, message = "Teklif adı en fazla 150 karakter olabilir.")
        String name,

        @NotNull(message = "Teknik özellik (productSpecId) zorunludur.")
        Long productSpecId,

        @NotNull(message = "Fiyat (price) zorunludur.")
        @DecimalMin(value = "0.0", message = "Fiyat negatif olamaz.")
        BigDecimal price,

        LocalDate startDate,

        LocalDate endDate,

        /** Opsiyonel: teklifin bağlanacağı katalog. */
        Long catalogId,

        /** Opsiyonel: teklifin bağlanacağı kampanya. */
        Long campaignId
) {
}
