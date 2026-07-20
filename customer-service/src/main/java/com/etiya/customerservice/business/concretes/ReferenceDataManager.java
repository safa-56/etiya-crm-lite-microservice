package com.etiya.customerservice.business.concretes;

import com.etiya.customerservice.business.abstracts.ReferenceDataService;
import com.etiya.customerservice.business.constants.Messages;
import com.etiya.customerservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.customerservice.dataAccess.GeneralStatusRepository;
import com.etiya.customerservice.dataAccess.GeneralTypeRepository;
import com.etiya.customerservice.dataAccess.PartyRoleTypeRepository;
import com.etiya.customerservice.entities.reference.GeneralStatus;
import com.etiya.customerservice.entities.reference.GeneralType;
import com.etiya.customerservice.entities.reference.PartyRoleType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Referans (lookup) verisi çözümleme servisi.
 *
 * <p>Sorgular küçük ve indeksli olduğundan Redis cache'lenmez; JPA entity'lerini
 * response cache'inde tutmak lazy-proxy/detached sorunları doğurur. İleride
 * ölçüm gerektirirse doğru araç Hibernate second-level cache olur.
 */
@Service
public class ReferenceDataManager implements ReferenceDataService {

    private final GeneralTypeRepository generalTypeRepository;
    private final GeneralStatusRepository generalStatusRepository;
    private final PartyRoleTypeRepository partyRoleTypeRepository;

    public ReferenceDataManager(GeneralTypeRepository generalTypeRepository,
                                GeneralStatusRepository generalStatusRepository,
                                PartyRoleTypeRepository partyRoleTypeRepository) {
        this.generalTypeRepository = generalTypeRepository;
        this.generalStatusRepository = generalStatusRepository;
        this.partyRoleTypeRepository = partyRoleTypeRepository;
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

    @Override
    @Transactional(readOnly = true)
    public PartyRoleType getPartyRoleType(String shortCode) {
        return partyRoleTypeRepository
                .findByShortCode(shortCode)
                .orElseThrow(() -> new BusinessException(
                        Messages.REFERENCE_DATA_NOT_FOUND + "party_role_types[" + shortCode + "]"));
    }
}
