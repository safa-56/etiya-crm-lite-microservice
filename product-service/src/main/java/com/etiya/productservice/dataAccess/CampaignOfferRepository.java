package com.etiya.productservice.dataAccess;

import com.etiya.productservice.entities.CampaignOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Kampanya–Teklif bağı (CampaignOffer) veri erişimi.
 */
@Repository
public interface CampaignOfferRepository extends JpaRepository<CampaignOffer, Long> {

    boolean existsByCampaignIdAndProductOfferIdAndDeletedDateIsNull(Long campaignId, Long productOfferId);

    /** Bir kampanyanın silinmemiş teklif bağları (paket içeriği). */
    List<CampaignOffer> findAllByCampaignIdAndDeletedDateIsNull(Long campaignId);
}
