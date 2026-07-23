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
        return createRoleForNewIndividual(PartyReferenceCodes.PARTY_ROLE_TYPE_CUSTOMER_CODE);
    }

    @Override
    @Transactional
    public PartyRole createUserRoleForIndividual() {
        return createRoleForNewIndividual(PartyReferenceCodes.PARTY_ROLE_TYPE_USER_CODE);
    }

    /**
     * Yeni bir bireysel party açıp verilen tipte aktif bir rol bağlar.
     * Müşteri ve kullanıcı rolleri yalnızca rol tipinde ayrıştığı için ortak tutulur.
     */
    private PartyRole createRoleForNewIndividual(String partyRoleTypeCode) {
        Party party = partyService.createIndividualParty();
        PartyRole partyRole = new PartyRole();

        partyRole.setParty(party);

        partyRole.setPartyRoleType(referenceDataService.getPartyRoleType(partyRoleTypeCode));

        partyRole.setGeneralStatus(referenceDataService.getStatus(
                PartyReferenceCodes.ENTITY_PARTY_ROLE, PartyReferenceCodes.STATUS_ACTIVE_CODE));

        return partyRoleRepository.save(partyRole);
    }

    @Override
    @Transactional
    public void deactivate(PartyRole partyRole) {
        if (partyRole == null) { return; }

        partyRole.setDeletedDate(LocalDateTime.now());
        partyRole.setGeneralStatus(referenceDataService.getStatus(
                PartyReferenceCodes.ENTITY_PARTY_ROLE, PartyReferenceCodes.STATUS_DELETED_CODE));

        partyRoleRepository.save(partyRole);

        // Bir party birden çok rol taşıyabilir (ör. aynı kişi hem CUST hem USER).
        // Party ancak SON aktif rolü de düştüğünde pasifleşir; aksi hâlde müşteri
        // kaydını silmek, aynı kişinin sisteme girişini de öldürürdü.
        // Sayım bu rol hariç yapılır: save() henüz flush edilmemiş olabilir.
        Party party = partyRole.getParty();
        long remainingActiveRoles =
                partyRoleRepository.countByPartyIdAndDeletedDateIsNullAndIdNot(party.getId(), partyRole.getId());

        if (remainingActiveRoles == 0) {
            partyService.deactivate(party);
        }
    }
}
