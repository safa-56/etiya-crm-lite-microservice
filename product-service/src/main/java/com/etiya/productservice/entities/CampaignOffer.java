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
 * Kampanya–Teklif bağı (CampaignOffer) — {@link Campaign} ile {@link ProductOffer}
 * arasındaki N-N ilişkiyi taşıyan birleşim (join) entity'si.
 *
 * <p>Bağın kampanyada halen geçerli olup olmadığı (aktif/pasif) durumu,
 * {@link StatusAwareEntity#getGeneralStatus()} FK'si üzerinden {@code general_status}
 * tablosuyla ifade edilir.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "campaign_offers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_campaign_offer",
                columnNames = {"campaign_id", "product_offer_id"}))
public class CampaignOffer extends StatusAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_offer_id", nullable = false)
    private ProductOffer productOffer;
}
