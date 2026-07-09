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
 * <p>Ürün tekliflerini gruplayan <b>kategoridir</b> (ör. Ev İnterneti, Mobil,
 * Superbox); Teklif Seçimi ekranının {@code Catalog} sekmesinde teklifleri
 * filtrelemek için kullanılır (FR-014). Bir katalog birden çok
 * {@link ProductOffer} içerir; her teklif <b>tam olarak bir</b> kataloğa aittir
 * ({@code ProductOffer.catalog} zorunlu FK — 1-N).
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
