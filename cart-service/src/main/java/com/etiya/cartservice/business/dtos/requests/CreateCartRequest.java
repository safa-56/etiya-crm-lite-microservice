package com.etiya.cartservice.business.dtos.requests;

import jakarta.validation.constraints.NotNull;

/**
 * Sepet oluşturma isteği.
 *
 * <p>Sepet bir müşteriye ({@code customerId}) ve o müşterinin bir fatura hesabına
 * ({@code accountId}) bağlıdır; ikisi de zorunludur. Bu kimlikler customer-service /
 * account-service kimlikleridir. Aynı müşteri + hesap için ikinci bir aktif sepet
 * açılması iş kuralıyla engellenir.
 */
public record CreateCartRequest(

        @NotNull(message = "Müşteri (customerId) zorunludur.")
        Long customerId,

        @NotNull(message = "Fatura hesabı (accountId) zorunludur.")
        Long accountId
) {
}
