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
 * <p>Bir veya daha fazla ürün teklifini <b>paket (bundle)</b> olarak sunar; Teklif
 * Seçimi ekranının {@code Campaign} sekmesinde listelenir (FR-014). Bir kampanya
 * seçildiğinde bağlı tüm teklifler ({@link CampaignOffer}) sepete <b>bir bütün
 * olarak</b> eklenir. {@code campaignPrice}, paketin tek satış fiyatıdır; tipik
 * olarak içindeki tekliflerin gerçek liste fiyatları toplamından düşüktür
 * (indirim). Bu düşüklük bir kural olarak <b>zorlanmaz</b>; fark
 * (savings = Σliste − campaignPrice) yalnızca yanıtta hesaplanıp gösterilir.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "campaigns")
public class Campaign extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** Paketin tek satış fiyatı (sepete bu fiyatla eklenir). */
    @Column(name = "campaign_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal campaignPrice;
}
