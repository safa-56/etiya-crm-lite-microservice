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
 * listelenir. Her teklif <b>zorunlu olarak</b> tek bir {@link Catalog}'a (kategori)
 * aittir; ayrıca opsiyonel olarak bir veya daha çok kampanyaya
 * ({@link CampaignOffer}) bağlanabilir. Müşteriye satıldığında
 * {@link Product} kaydına dönüşür.
 *
 * <p>ERD'deki {@code update_date} alanı {@link BaseEntity#getUpdatedDate()} ile,
 * durum bilgisi (aktif/pasif) ise {@link StatusAwareEntity#getGeneralStatus()}
 * FK'si üzerinden {@code general_status} tablosuyla karşılanır.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "product_offers")
public class ProductOffer extends StatusAwareEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** Teklifin ait olduğu katalog (kategori). Zorunlu — her teklifin bir kataloğu olur. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "catalog_id", nullable = false)
    private Catalog catalog;

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
