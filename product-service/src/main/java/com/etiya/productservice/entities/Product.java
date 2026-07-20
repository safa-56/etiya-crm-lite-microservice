package com.etiya.productservice.entities;

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
 * Ürün (Product) entity'si — bir {@link ProductOffer}'ın <b>satılmış son hali</b>.
 *
 * <p>Teklif, müşterinin fatura hesabıyla ({@code accountId}) bağdaştırıldığında bu
 * kayda dönüşür: hangi fatura hesabına bağlı olduğu, hangi fiyata satıldığı
 * ({@code priceToBePaid}), varsa hangi kampanyaya ({@link Campaign}) ve hangi
 * servis adresine ({@code addressId}) ait olduğu burada tutulur. Teknik özellikler
 * ve teklif adı, bağlı {@link ProductOffer} → {@link ProductSpec} üzerinden gelir.
 *
 * <p>{@code accountId} account-service'teki fatura hesabına, {@code addressId}
 * customer-service'teki adrese <b>yerel FK olmayan</b> servisler arası
 * referanslardır (Customer ↔ Account iletişim modeliyle aynı).
 *
 * <p>Satış bir <b>choreography Saga</b> ile kesinleşir: ürün {@code PNDG} (Beklemede)
 * açılır, account-service fatura hesabını doğrulayınca {@code ACTV} (onay) ya da
 * {@code QUOTE_DEL} (telafi/iptal) olur. Bu durum, {@link StatusAwareEntity}
 * üzerinden {@code general_status} tablosuna FK ile taşınır; {@code statusReason}
 * yalnızca telafi nedenini (gözlemlenebilirlik) tutan serbest metindir.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product extends StatusAwareEntity {

    /** Ürün Detayı ekranında gösterilen ürün adı (FR-013 "Product Name"). */
    @Column(name = "name", length = 150)
    private String name;

    /** Ürünün kaynaklandığı, satılan teklif. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_offer_id", nullable = false)
    private ProductOffer productOffer;

    /** Ürünün bağlı olduğu fatura hesabı (account-service billing account id'si). */
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    /** Varsa ürünün ait olduğu kampanya (opsiyonel). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    /** Servis adresi (customer-service adres id'sine referans). */
    @Column(name = "address_id")
    private Long addressId;

    /** Ürünün müşteriye satıldığı, ödenecek nihai fiyat. */
    @Column(name = "price_to_be_paid", nullable = false, precision = 19, scale = 2)
    private BigDecimal priceToBePaid;

    /**
     * Saga sonucu açıklaması (gözlemlenebilirlik). Doğrulama başarısız olup ürün
     * CANCELLED yapıldığında iptal nedenini tutar; başarılı akışta {@code null}.
     */
    @Column(name = "status_reason", length = 500)
    private String statusReason;
}
