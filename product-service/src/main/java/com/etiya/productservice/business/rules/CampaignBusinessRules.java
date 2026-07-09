package com.etiya.productservice.business.rules;

import com.etiya.productservice.business.constants.Messages;
import com.etiya.productservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.productservice.dataAccess.CampaignRepository;
import org.springframework.stereotype.Service;

/**
 * Kampanya iş kuralları.
 */
@Service
public class CampaignBusinessRules {

    private final CampaignRepository campaignRepository;

    public CampaignBusinessRules(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    /** Aktif bir kampanya id ile var olmalı; yoksa iş hatası fırlatılır. */
    public void checkIfCampaignExists(Long id) {
        if (!campaignRepository.existsByIdAndIsActiveTrue(id)) {
            throw new BusinessException(Messages.CAMPAIGN_NOT_FOUND);
        }
    }
}
