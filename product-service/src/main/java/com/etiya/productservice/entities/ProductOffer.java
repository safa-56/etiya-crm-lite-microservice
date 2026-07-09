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
import java.time.LocalDate;

/**
 * Ürün teklifi (ProductOffer) entity'si.
 *
 * <p>Bir {@link ProductSpec}'i <b>fiyatlandıran</b> tekliftir; teklif seçimi
 * (FR-014) ekranında {@code Prod Offer ID} / {@code Prod Offer Name} olarak
 * listelenir. Bir teklif bir kataloga ({@link CatalogOffer}) ve/veya bir
 * kampanyaya ({@link CampaignOffer}) bağlanabilir. Müşteriye satıldığında
 * {@link Product} kaydına dönüşür.
 *
 * <p>ERD'deki {@code is_active} ve {@code update_date} alanları {@link BaseEntity}
 * üzerindeki {@code isActive} ve {@code updatedDate} alanlarıyla karşılanır.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "product_offers")
public class ProductOffer extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** Teklifin fiyatlandırdığı teknik özellik. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_spec_id", nullable = false)
    private ProductSpec productSpec;

    /** Teklifin geçerlilik başlangıç tarihi. */
    @Column(name = "start_date")
    private LocalDate startDate;

    /** Teklifin geçerlilik bitiş tarihi. */
    @Column(name = "end_date")
    private LocalDate endDate;

    /** Teklifin liste fiyatı. */
    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;
}
