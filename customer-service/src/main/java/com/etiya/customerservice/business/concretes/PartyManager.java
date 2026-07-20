package com.etiya.customerservice.business.concretes;

import com.etiya.customerservice.business.abstracts.PartyService;
import com.etiya.customerservice.business.abstracts.ReferenceDataService;
import com.etiya.customerservice.business.constants.PartyReferenceCodes;
import com.etiya.customerservice.dataAccess.PartyRepository;
import com.etiya.customerservice.entities.Party;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Party iş kuralları.
 *
 * <p>Referans verisine doğrudan repository ile değil {@link ReferenceDataService}
 * üzerinden erişir; kendi aggregate'i olan {@code parties} için kendi
 * repository'sini kullanır.
 */
@Service
public class PartyManager implements PartyService {

    private final PartyRepository partyRepository;
    private final ReferenceDataService referenceDataService;

    public PartyManager(PartyRepository partyRepository,
                        ReferenceDataService referenceDataService) {
        this.partyRepository = partyRepository;
        this.referenceDataService = referenceDataService;
    }

    @Override
    @Transactional
    public Party createIndividualParty() {
        Party party = new Party();
        party.setPartyType(referenceDataService.getType(
                PartyReferenceCodes.ENTITY_CAM_PARTY_TYPE, PartyReferenceCodes.PARTY_TYPE_INDIVIDUAL_CODE));
        party.setGeneralStatus(referenceDataService.getStatus(
                PartyReferenceCodes.ENTITY_PARTY, PartyReferenceCodes.STATUS_ACTIVE_CODE));
        return partyRepository.save(party);
    }

    @Override
    @Transactional
    public void deactivate(Party party) {
        if (party == null) {
            return;
        }
        party.setDeletedDate(LocalDateTime.now());
        party.setGeneralStatus(referenceDataService.getStatus(
                PartyReferenceCodes.ENTITY_PARTY, PartyReferenceCodes.STATUS_DELETED_CODE));
        partyRepository.save(party);
    }
}
