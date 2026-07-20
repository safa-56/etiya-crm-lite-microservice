package com.etiya.cartservice.business.concretes;

import com.etiya.cartservice.business.abstracts.ReferenceDataService;
import com.etiya.cartservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.cartservice.dataAccess.GeneralStatusRepository;
import com.etiya.cartservice.entities.reference.GeneralStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Referans (lookup) verisi çözümleme servisi.
 *
 * <p>Sorgular küçük ve indeksli olduğundan Redis cache'lenmez; JPA entity'lerini
 * response cache'inde tutmak lazy-proxy/detached sorunları doğurur.
 */
@Service
public class ReferenceDataManager implements ReferenceDataService {

    private final GeneralStatusRepository generalStatusRepository;

    public ReferenceDataManager(GeneralStatusRepository generalStatusRepository) {
        this.generalStatusRepository = generalStatusRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralStatus getStatus(String entityCodeName, String shortCode) {
        return generalStatusRepository
                .findByEntityCodeNameAndShortCode(entityCodeName, shortCode)
                .orElseThrow(() -> new BusinessException(
                        "Referans veri bulunamadı: general_status[" + entityCodeName + "/" + shortCode + "]"));
    }
}
