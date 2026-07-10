package com.etiya.orderservice.business.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Sipariş oluşturma (Submit Order) isteği — FR-016.
 *
 * <p>Kullanıcı, Product Configuration adımından gelen bir sepeti onayladığında bu istek
 * gönderilir. {@code cartId} onaylanacak sepettir (kalemler ve toplam bu sepetin otoriter
 * içeriğinden — cart-service doğrulaması ile — alınır). {@code serviceAddressId} ve
 * {@code serviceAddress}, FR-015 (Product Configuration) adımında seçilen/eklenen servis
 * adresidir; adres metni Submit Order ekranında gösterileceğinden zorunludur.
 *
 * <p>Müşteri ve fatura hesabı kimlikleri istekte alınmaz; sepetin otoriter içeriğiyle
 * birlikte cart-service doğrulama sonucundan gelir.
 */
public record SubmitOrderRequest(

        @NotNull(message = "Sepet (cartId) zorunludur.")
        Long cartId,

        // customer-service adres kimliği (opsiyonel referans; yeni eklenen adreste dolar).
        Long serviceAddressId,

        @NotBlank(message = "Servis adresi (serviceAddress) zorunludur.")
        @Size(max = 500, message = "Servis adresi en fazla 500 karakter olabilir.")
        String serviceAddress
) {
}
