package com.etiya.productservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Kampanya (Campaign) entity'si.
 *
 * <p>Bir veya daha fazla ürün teklifini paket olarak sunar; Teklif Seçimi
 * ekranının {@code Campaign} sekmesinde listelenir (FR-014). Bir kampanya
 * seçildiğinde bağlı tüm teklifler ({@link CampaignOffer}) sepete eklenir.
 * {@code totalPrice}, kampanyaya bağlı tekliflerin toplam paket fiyatıdır.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "campaigns")
public class Campaign extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "total_price", precision = 19, scale = 2)
    private BigDecimal totalPrice;
}
