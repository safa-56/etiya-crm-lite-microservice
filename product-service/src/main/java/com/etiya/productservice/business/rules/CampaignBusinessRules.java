package com.etiya.productservice.business.rules;

import com.etiya.productservice.business.constants.Messages;
import com.etiya.productservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.productservice.dataAccess.CampaignRepository;
import com.etiya.productservice.dataAccess.ProductOfferRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Kampanya iş kuralları.
 */
@Service
public class CampaignBusinessRules {

    private final CampaignRepository campaignRepository;
    private final ProductOfferRepository productOfferRepository;

    public CampaignBusinessRules(CampaignRepository campaignRepository,
                                 ProductOfferRepository productOfferRepository) {
        this.campaignRepository = campaignRepository;
        this.productOfferRepository = productOfferRepository;
    }

    /** Aktif bir kampanya id ile var olmalı; yoksa iş hatası fırlatılır. */
    public void checkIfCampaignExists(Long id) {
        if (!campaignRepository.existsByIdAndDeletedDateIsNull(id)) {
            throw new BusinessException(Messages.CAMPAIGN_NOT_FOUND);
        }
    }

    /**
     * Paketin içerdiği teklif id'lerinin tümü aktif ve var olmalıdır; ayrıca aynı
     * teklif pakette birden çok kez yer alamaz.
     */
    public void checkOffersValidForCampaign(List<Long> offerIds) {
        List<Long> distinct = offerIds.stream().distinct().toList();
        if (distinct.size() != offerIds.size()) {
            throw new BusinessException(Messages.CAMPAIGN_DUPLICATE_OFFER);
        }
        long foundActive = productOfferRepository.findAllByIdInAndDeletedDateIsNull(distinct).size();
        if (foundActive != distinct.size()) {
            throw new BusinessException(Messages.PRODUCT_OFFER_NOT_FOUND);
        }
    }
}
