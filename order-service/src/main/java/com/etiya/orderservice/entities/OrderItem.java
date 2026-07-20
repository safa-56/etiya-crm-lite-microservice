package com.etiya.orderservice.entities;

import com.etiya.orderservice.entities.enums.OrderItemType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Sipariş satırı (OrderItem) entity'si — sipariş anındaki sepet kaleminin snapshot'ı.
 *
 * <p>FR-016 (ACC-03) gereği Order Items alanında sepetteki her ürün teklifinin/kampanyanın
 * kimliği ve adı listelenir. Bu satır, cart-service'in doğrulama olayıyla gönderdiği
 * kalem bilgisini (ad, birim fiyat, adet) <b>snapshot</b> olarak tutar; sepet sonradan
 * değişse bile sipariş kaydı sabit kalır. {@code productOfferId} / {@code campaignId}
 * product-service kimlikleridir (per-service DB gereği FK değil ham referans).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem extends StatusAwareEntity {

    /** Satırın ait olduğu sipariş. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** Kalem türü: OFFER (tek teklif) ya da CAMPAIGN (paket). */
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private OrderItemType itemType;

    /** Ürün teklifi kimliği (OFFER satırlarında dolu; CAMPAIGN satırlarında boş). */
    @Column(name = "product_offer_id")
    private Long productOfferId;

    /** Kampanya kimliği (CAMPAIGN satırlarında dolu; OFFER satırlarında boş). */
    @Column(name = "campaign_id")
    private Long campaignId;

    /** Kalemin adı — sepet doğrulamasıyla gelen snapshot. */
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** Birim fiyat snapshot'ı. */
    @Column(name = "unit_price", precision = 19, scale = 2)
    private BigDecimal unitPrice;

    /** Adet. */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /** Bu satırın ara toplamı: fiyat yoksa 0; aksi halde {@code unitPrice * quantity}. */
    public BigDecimal lineTotal() {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity == null ? 0 : quantity));
    }
}
