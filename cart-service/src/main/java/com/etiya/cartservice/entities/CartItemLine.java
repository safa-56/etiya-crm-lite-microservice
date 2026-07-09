package com.etiya.cartservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Bir CAMPAIGN sepet satırının paket içeriğindeki tek teklif (snapshot).
 *
 * <p>Kampanya sepete "bir bütün olarak" eklenir; paketin hangi tekliflerden oluştuğu,
 * Saga doğrulama olayıyla product-service'ten gelen <b>snapshot</b> olarak bu tabloda
 * saklanır (yerel projeksiyon yoktur — veri olay üzerinden akar). Sepet yanıtında paket
 * içeriğini göstermek için kullanılır.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cart_item_lines")
public class CartItemLine extends BaseEntity {

    /** Bu satırın ait olduğu sepet satırı (kampanya). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_item_id", nullable = false)
    private CartItem cartItem;

    /** Paketteki teklifin kaynak (product-service) kimliği. */
    @Column(name = "offer_id", nullable = false)
    private Long offerId;

    @Column(name = "offer_name", nullable = false, length = 150)
    private String offerName;

    /** Teklifin (kampanya dışı) liste fiyatı — gösterim için. */
    @Column(name = "list_price", precision = 19, scale = 2)
    private BigDecimal listPrice;
}
