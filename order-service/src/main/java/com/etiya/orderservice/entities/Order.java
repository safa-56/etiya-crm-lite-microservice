package com.etiya.orderservice.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Sipariş (Order) entity'si — FR-016 "Siparişin Tamamlanması" çıktısı.
 *
 * <p>Bir sipariş, bir sepetin ({@code cartId}) onaylanmış (checkout edilmiş) hâlidir.
 * {@code orderNumber} sistem tarafından üretilen benzersiz sipariş kimliğidir
 * (Submit Order ekranında gösterilir). {@code customerId} / {@code accountId} sepetin
 * sahiplik bilgisidir; {@code serviceAddress} FR-015 (Product Configuration) adımında
 * seçilen servis adresinin metin snapshot'ıdır. Sepet kalemleri sipariş anında
 * {@link OrderItem} satırlarına snapshot'lanır, {@code totalAmount} = Σ satır ara toplamı.
 *
 * <p>Sipariş bir <b>Saga</b> ile kesinleşir: oluşturma sırasında {@code CUST_ORD/MIDLWARE}
 * (işleniyor) açılır (satırlar/toplam boş), cart-service doğrulaması geldiğinde satır/toplam
 * snapshot'ı yazılır ve durum {@code FINISHED} olur; sepet yoksa/boşsa telafi ile
 * {@code REJECTED} olur. Durum {@link StatusAwareEntity#getGeneralStatus()} FK'siyle
 * {@code general_status} tablosunda tutulur.
 *
 * <p>Silme soft-delete'tir: durum {@code CUST_ORD/DEL} yapılır + {@code deletedDate} yazılır.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order extends StatusAwareEntity {

    /** Sistem tarafından üretilen benzersiz sipariş numarası (Order ID). Zorunlu. */
    @Column(name = "order_number", nullable = false, unique = true, length = 40)
    private String orderNumber;

    /** Siparişin kaynağı sepet (cart-service kimliği). Zorunlu. */
    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    /** Siparişin sahibi müşteri (customer-service kimliği) — sepet doğrulamasıyla gelir. */
    @Column(name = "customer_id")
    private Long customerId;

    /** Siparişin bağlı olduğu fatura hesabı (account-service kimliği) — sepet doğrulamasıyla gelir. */
    @Column(name = "account_id")
    private Long accountId;

    /** Servis adresi kimliği (customer-service adres kimliği) — FR-015'te seçilen adres. */
    @Column(name = "service_address_id")
    private Long serviceAddressId;

    /** Servis adresi metin snapshot'ı (Submit Order ekranında gösterilir). Zorunlu. */
    @Column(name = "service_address", nullable = false, length = 500)
    private String serviceAddress;

    /** Doğrulama başarısızsa telafi nedeni (aksi halde boş). */
    @Column(name = "status_reason", length = 300)
    private String statusReason;

    /** Sipariş toplam tutarı — sepet doğrulamasıyla yazılan snapshot (PENDING iken boş). */
    @Column(name = "total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Siparişin kalemleri. Siparişle yaşam döngüsü ortaktır (cascade);
     * yönü {@link OrderItem#getOrder()} üzerinden yönetilir.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    /** İki yönlü ilişkiyi tutarlı kuran yardımcı: kalemi siparişe ekler. */
    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
    }
}
