package com.etiya.productservice.business.concretes;

import com.etiya.productservice.business.abstracts.ReferenceDataService;
import com.etiya.productservice.business.constants.Messages;
import com.etiya.productservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.productservice.dataAccess.GeneralStatusRepository;
import com.etiya.productservice.dataAccess.GeneralTypeRepository;
import com.etiya.productservice.entities.reference.GeneralStatus;
import com.etiya.productservice.entities.reference.GeneralType;
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

    private final GeneralTypeRepository generalTypeRepository;
    private final GeneralStatusRepository generalStatusRepository;

    public ReferenceDataManager(GeneralTypeRepository generalTypeRepository,
                                GeneralStatusRepository generalStatusRepository) {
        this.generalTypeRepository = generalTypeRepository;
        this.generalStatusRepository = generalStatusRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralType getType(String entityCodeName, String shortCode) {
        return generalTypeRepository
                .findByEntityCodeNameAndShortCode(entityCodeName, shortCode)
                .orElseThrow(() -> new BusinessException(
                        Messages.REFERENCE_DATA_NOT_FOUND + "general_type[" + entityCodeName + "/" + shortCode + "]"));
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralStatus getStatus(String entityCodeName, String shortCode) {
        return generalStatusRepository
                .findByEntityCodeNameAndShortCode(entityCodeName, shortCode)
                .orElseThrow(() -> new BusinessException(
                        Messages.REFERENCE_DATA_NOT_FOUND + "general_status[" + entityCodeName + "/" + shortCode + "]"));
    }
}
