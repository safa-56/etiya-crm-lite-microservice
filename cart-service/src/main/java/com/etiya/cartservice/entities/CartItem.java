package com.etiya.cartservice.entities;

import com.etiya.cartservice.entities.enums.CartItemStatus;
import com.etiya.cartservice.entities.enums.CartItemType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Sepet satırı (CartItem) entity'si.
 *
 * <p>ERD'deki {@code CartItems} tablosunu karşılar. Bir satır ya bir <b>ürün teklifi</b>
 * (OFFER) ya da bir <b>kampanya</b> (CAMPAIGN) referansı taşır — hangisi olduğu
 * {@link #itemType} ile belirlenir. {@code productOfferId} ve {@code campaignId},
 * product-service kimlikleridir (per-service DB gereği FK değil ham referans).
 *
 * <p>Satır bir <b>Saga</b> ile kesinleşir: ekleme sırasında {@link CartItemStatus#PENDING}
 * açılır ({@code name}/{@code unitPrice} henüz boştur), product-service doğrulaması
 * geldiğinde ad/fiyat <b>snapshot</b>'ı yazılır ve durum ACTIVE olur; doğrulanamazsa
 * telafi ile CANCELLED olur. CAMPAIGN satırında paketin içeriği, doğrulama olayından
 * gelen snapshot satırlarında ({@link CartItemLine}) tutulur.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cart_items")
public class CartItem extends BaseEntity {

    /** Satırın ait olduğu sepet. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    /** Satır türü: OFFER (tek teklif) ya da CAMPAIGN (paket). */
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private CartItemType itemType;

    /** Saga durumu: PENDING → ACTIVE | CANCELLED. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CartItemStatus status;

    /** Doğrulama başarısızsa telafi nedeni (aksi halde boş). */
    @Column(name = "status_reason", length = 300)
    private String statusReason;

    /** Ürün teklifi kimliği (OFFER satırlarında dolu; CAMPAIGN satırlarında boş). */
    @Column(name = "product_offer_id")
    private Long productOfferId;

    /** Kampanya kimliği (CAMPAIGN satırlarında dolu; OFFER satırlarında boş). */
    @Column(name = "campaign_id")
    private Long campaignId;

    /** Satırın görünen adı — Saga doğrulamasıyla product-service'ten gelen snapshot. */
    @Column(name = "name", length = 150)
    private String name;

    /** Birim fiyat snapshot'ı — Saga doğrulamasıyla yazılır (PENDING iken boş). */
    @Column(name = "unit_price", precision = 19, scale = 2)
    private BigDecimal unitPrice;

    /** Adet (varsayılan 1). Kampanya satırlarında da 1 tutulur (paket bütünlüğü). */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /** CAMPAIGN satırında paketin içeriği (doğrulama olayından gelen snapshot). */
    @OneToMany(mappedBy = "cartItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItemLine> lines = new ArrayList<>();

    /** İki yönlü ilişkiyi tutarlı kuran yardımcı: paket satırını sepet satırına ekler. */
    public void addLine(CartItemLine line) {
        line.setCartItem(this);
        this.lines.add(line);
    }

    /** Bu satırın ara toplamı: fiyat henüz yoksa (PENDING) sıfır; aksi halde {@code unitPrice * quantity}. */
    public BigDecimal lineTotal() {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
