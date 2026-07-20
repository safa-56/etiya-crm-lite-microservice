package com.etiya.customerservice.business.concretes;

import com.etiya.customerservice.business.abstracts.PartyRoleService;
import com.etiya.customerservice.business.abstracts.PartyService;
import com.etiya.customerservice.business.abstracts.ReferenceDataService;
import com.etiya.customerservice.business.constants.PartyReferenceCodes;
import com.etiya.customerservice.dataAccess.PartyRoleRepository;
import com.etiya.customerservice.entities.Party;
import com.etiya.customerservice.entities.PartyRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Party rolü iş kuralları.
 *
 * <p>Party oluşturmak için {@link PartyService}'e bağlanır (yabancı repository'ye
 * değil); kendi aggregate'i {@code party_roles} için kendi repository'sini kullanır.
 */
@Service
public class PartyRoleManager implements PartyRoleService {

    private final PartyRoleRepository partyRoleRepository;
    private final PartyService partyService;
    private final ReferenceDataService referenceDataService;

    public PartyRoleManager(PartyRoleRepository partyRoleRepository,
                            PartyService partyService,
                            ReferenceDataService referenceDataService) {
        this.partyRoleRepository = partyRoleRepository;
        this.partyService = partyService;
        this.referenceDataService = referenceDataService;
    }

    @Override
    @Transactional
    public PartyRole createCustomerRoleForIndividual() {
        Party party = partyService.createIndividualParty();

        PartyRole partyRole = new PartyRole();
        partyRole.setParty(party);
        partyRole.setPartyRoleType(referenceDataService.getPartyRoleType(
                PartyReferenceCodes.PARTY_ROLE_TYPE_CUSTOMER_CODE));
        partyRole.setGeneralStatus(referenceDataService.getStatus(
                PartyReferenceCodes.ENTITY_PARTY_ROLE, PartyReferenceCodes.STATUS_ACTIVE_CODE));

        return partyRoleRepository.save(partyRole);
    }

    @Override
    @Transactional
    public void deactivate(PartyRole partyRole) {
        if (partyRole == null) {
            return;
        }
        partyRole.setDeletedDate(LocalDateTime.now());
        partyRole.setGeneralStatus(referenceDataService.getStatus(
                PartyReferenceCodes.ENTITY_PARTY_ROLE, PartyReferenceCodes.STATUS_DELETED_CODE));
        partyRoleRepository.save(partyRole);

        // Bu projede bir party yalnızca müşteri rolü oynadığından, rol pasifleşince
        // party de pasifleşir. Party'ye ikinci bir rol eklenirse bu kural gözden
        // geçirilmelidir (diğer roller aktifken party pasifleştirilmemeli).
        partyService.deactivate(partyRole.getParty());
    }
}
