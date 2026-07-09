package com.etiya.productservice.dataAccess;

import com.etiya.productservice.entities.CampaignOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Kampanya–Teklif bağı (CampaignOffer) veri erişimi.
 */
@Repository
public interface CampaignOfferRepository extends JpaRepository<CampaignOffer, Long> {

    boolean existsByCampaignIdAndProductOfferIdAndIsActiveTrue(Long campaignId, Long productOfferId);
}
