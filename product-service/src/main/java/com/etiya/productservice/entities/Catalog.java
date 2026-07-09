package com.etiya.productservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Katalog (Catalog) entity'si.
 *
 * <p>Ürün tekliflerini gruplayan katalogdur; Teklif Seçimi ekranının
 * {@code Catalog} sekmesinde teklifleri filtrelemek için kullanılır (FR-014).
 * Bir katalog, {@link CatalogOffer} üzerinden birden çok {@link ProductOffer}
 * içerir (N-N).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "catalogs")
public class Catalog extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;
}
