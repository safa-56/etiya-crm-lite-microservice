package com.etiya.productservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ürün teknik özelliği (ProductSpec) entity'si.
 *
 * <p>Bir ürünün <b>teknik özelliklerini ve detaylarını</b> tanımlar (fiyattan
 * bağımsız). Ürün Detayı ekranında {@code Product Spec ID} ve {@code Product Char}
 * bilgilerinin kaynağıdır (FR-013). Bir spec, bir veya daha fazla
 * {@link ProductOffer} tarafından fiyatlandırılabilir (1-N).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "product_specs")
public class ProductSpec extends StatusAwareEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;
}
