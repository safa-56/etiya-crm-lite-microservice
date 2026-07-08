package com.etiya.accountservice.entities;

import com.etiya.accountservice.entities.enums.AccountStatus;
import com.etiya.accountservice.entities.enums.AccountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Fatura hesabı (BillingAccount) entity'si.
 *
 * <p>Bir müşteriye ({@code customerId}) bağlıdır. Kabul kriterleri:
 * <ul>
 *   <li>{@code accountNumber}: alfanümerik, en fazla 30 karakter.</li>
 *   <li>{@code orderNumber}: alfanümerik, en fazla 20 karakter.</li>
 *   <li>Yeni kayıtta {@code accountType = BILLING_ACCOUNT}, {@code accountStatus = ACTIVE}.</li>
 *   <li>Silme fizikseldir değildir: {@code isActive=false} + {@code accountStatus=PASSIVE}.</li>
 * </ul>
 *
 * <p>{@code activeProductCount}, hesaba bağlı aktif ürün sayısını tutan yerel
 * projeksiyondur; product-service'in ürün olaylarını (Inbox Pattern ile) tüketerek
 * güncellenir. Silme iş kuralı bu alana bakar ("aktif ürünü varsa silinemez").
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "billing_accounts")
public class BillingAccount extends BaseEntity {

    /** Hesabın bağlı olduğu müşteri (customer-service'teki müşteri id'si). */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "account_name", nullable = false, length = 150)
    private String accountName;

    @Column(name = "account_description", length = 500)
    private String accountDescription;

    /**
     * Hesabın adresi, müşterinin adreslerinden biriyle ({@link #addressId})
     * ilişkilidir; seçilen adres yerel müşteri projeksiyonundan doğrulanır.
     * customer-service adres id'sine referans (yerel FK değil).
     */
    @Column(name = "address_id")
    private Long addressId;

    /**
     * Seçilen adresin okunur metin gösterimi (snapshot). Saga PENDING aşamasında
     * henüz doğrulanmadığından {@code null} olabilir; customer-service doğrulaması
     * (CustomerValidated) geldiğinde otoriter değerle doldurulur.
     */
    @Column(name = "address", length = 500)
    private String address;

    /** Alfanümerik, en fazla 30 karakter. */
    @Column(name = "account_number", length = 30)
    private String accountNumber;

    /** Alfanümerik, en fazla 20 karakter. */
    @Column(name = "order_number", length = 20)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 30)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 20)
    private AccountStatus accountStatus;

    /** Hesaba bağlı aktif ürün sayısı (ürün olaylarından türetilir). */
    @Column(name = "active_product_count", nullable = false)
    private Integer activeProductCount = 0;

    /**
     * Saga sonucu açıklaması (gözlemlenebilirlik). Doğrulama başarısız olup hesap
     * CANCELLED yapıldığında iptal nedenini tutar; başarılı akışta {@code null}.
     */
    @Column(name = "status_reason", length = 500)
    private String statusReason;
}
