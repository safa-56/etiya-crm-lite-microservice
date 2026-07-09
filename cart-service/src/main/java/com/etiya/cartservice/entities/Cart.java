package com.etiya.cartservice.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Sepet (Cart) entity'si.
 *
 * <p>ERD'deki {@code Carts} tablosunu birebir karşılar: bir sepet tek bir müşteriye
 * ({@code customerId}) ve o müşterinin bir fatura hesabına ({@code accountId}) aittir.
 * Bu iki alan customer-service / account-service kimlikleridir; servisler-arası
 * gevşek bağlılık için burada FK değil ham referans (Long) olarak tutulur (per-service
 * DB ilkesi). Sepetin içeriği {@link CartItem} satırlarında yaşar.
 *
 * <p>Silme soft-delete'tir ({@link BaseEntity#getIsActive()}); "sepeti boşalt"
 * işlemi sepeti ve tüm satırlarını pasifleştirir.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "carts")
public class Cart extends BaseEntity {

    /** Sepetin sahibi müşteri (customer-service kimliği). Zorunlu. */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /** Sepetin bağlı olduğu fatura hesabı (account-service kimliği). Zorunlu. */
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    /**
     * Sepetteki satırlar. Sepetle yaşam döngüsü ortaktır (cascade);
     * yönü {@link CartItem#getCart()} üzerinden yönetilir.
     */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    /** İki yönlü ilişkiyi tutarlı kuran yardımcı: satırı sepete ekler. */
    public void addItem(CartItem item) {
        item.setCart(this);
        this.items.add(item);
    }
}
