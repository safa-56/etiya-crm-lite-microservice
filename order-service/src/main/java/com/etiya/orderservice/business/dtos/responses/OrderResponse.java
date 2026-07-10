package com.etiya.orderservice.business.dtos.responses;

import com.etiya.orderservice.entities.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Sipariş yanıtı — Submit Order ekranının (FR-016) alanlarını karşılar.
 *
 * <p>{@code orderNumber} sistem tarafından üretilen benzersiz sipariş kimliğidir (ACC-02);
 * {@code items} sepetteki tüm ürün tekliflerini/kampanyaları listeler (ACC-03);
 * {@code serviceAddress} seçilen servis adresidir (ACC-04); {@code totalAmount} sepet
 * toplamıdır (ACC-05). Sipariş bir Saga ile kesinleştiğinden {@code status}
 * (PENDING/CONFIRMED/CANCELLED) de döner: yeni sipariş kısa süre PENDING görünür,
 * cart-service doğrulaması gelince CONFIRMED olur (satırlar/toplam dolar).
 *
 * @param id             sipariş kimliği
 * @param orderNumber    benzersiz sipariş numarası (Order ID)
 * @param cartId         kaynak sepet kimliği
 * @param customerId     müşteri kimliği
 * @param accountId      fatura hesabı kimliği
 * @param serviceAddressId servis adresi kimliği (opsiyonel)
 * @param serviceAddress servis adresi metni
 * @param status         saga durumu
 * @param statusReason   telafi nedeni (varsa)
 * @param items          sipariş satırları
 * @param totalAmount    sipariş toplam tutarı
 * @param isActive       aktif mi (soft-delete durumu)
 * @param createdDate    oluşturulma zamanı
 * @param updatedDate    son güncellenme zamanı
 */
public record OrderResponse(
        Long id,
        String orderNumber,
        Long cartId,
        Long customerId,
        Long accountId,
        Long serviceAddressId,
        String serviceAddress,
        OrderStatus status,
        String statusReason,
        List<OrderItemResponse> items,
        BigDecimal totalAmount,
        Boolean isActive,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
