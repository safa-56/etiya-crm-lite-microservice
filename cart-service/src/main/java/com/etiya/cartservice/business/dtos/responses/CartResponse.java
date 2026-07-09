package com.etiya.cartservice.business.dtos.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Sepet yanıtı.
 *
 * <p>Sepetin kimlik/sahiplik bilgisiyle birlikte satırlarını ({@code items}) ve
 * türetilmiş toplam tutarını ({@code totalPrice} = Σ satır ara toplamı) döner.
 *
 * @param id         sepet kimliği
 * @param customerId müşteri kimliği
 * @param accountId  fatura hesabı kimliği
 * @param items      sepet satırları
 * @param totalPrice sepet toplam tutarı
 * @param isActive   sepet aktif mi (soft-delete durumu)
 * @param createdDate oluşturulma zamanı
 * @param updatedDate son güncellenme zamanı
 */
public record CartResponse(
        Long id,
        Long customerId,
        Long accountId,
        List<CartItemResponse> items,
        BigDecimal totalPrice,
        Boolean isActive,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
