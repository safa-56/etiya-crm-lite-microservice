package com.etiya.productservice.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Katalog–Teklif bağı (CatalogOffer) — {@link Catalog} ile {@link ProductOffer}
 * arasındaki N-N ilişkiyi taşıyan birleşim (join) entity'si.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "catalog_offers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_catalog_offer",
                columnNames = {"catalog_id", "product_offer_id"}))
public class CatalogOffer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "catalog_id", nullable = false)
    private Catalog catalog;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_offer_id", nullable = false)
    private ProductOffer productOffer;
}
