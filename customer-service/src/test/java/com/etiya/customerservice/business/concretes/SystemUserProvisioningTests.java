package com.etiya.customerservice.business.concretes;

import com.etiya.customerservice.business.abstracts.PartyRoleService;
import com.etiya.customerservice.business.abstracts.SystemUserService;
import com.etiya.customerservice.business.constants.PartyReferenceCodes;
import com.etiya.customerservice.dataAccess.GeneralStatusRepository;
import com.etiya.customerservice.dataAccess.GeneralTypeRepository;
import com.etiya.customerservice.dataAccess.PartyRepository;
import com.etiya.customerservice.dataAccess.PartyRoleRepository;
import com.etiya.customerservice.dataAccess.PartyRoleTypeRepository;
import com.etiya.customerservice.dataAccess.SystemUserRepository;
import com.etiya.customerservice.entities.Party;
import com.etiya.customerservice.entities.PartyRole;
import com.etiya.customerservice.entities.SystemUser;
import com.etiya.customerservice.entities.reference.GeneralStatus;
import com.etiya.customerservice.entities.reference.GeneralType;
import com.etiya.customerservice.entities.reference.PartyRoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Keycloak kullanıcısının party zincirine bağlanmasını (lazy provisioning) ve
 * çok rollü party'lerin pasifleştirme kuralını doğrular.
 *
 * <p>Hermetik test profili {@code data.sql}'i çalıştırmadığından, provizyonun
 * ihtiyaç duyduğu referans veri burada elle tohumlanır.
 */
@SpringBootTest
@ActiveProfiles("test")
class SystemUserProvisioningTests {

    @Autowired
    private SystemUserService systemUserService;

    @Autowired
    private PartyRoleService partyRoleService;

    @Autowired
    private SystemUserRepository systemUserRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private PartyRoleRepository partyRoleRepository;

    @Autowired
    private GeneralStatusRepository generalStatusRepository;

    @Autowired
    private GeneralTypeRepository generalTypeRepository;

    @Autowired
    private PartyRoleTypeRepository partyRoleTypeRepository;

    @BeforeEach
    void seedReferenceData() {
        seedStatus(PartyReferenceCodes.ENTITY_PARTY, PartyReferenceCodes.STATUS_ACTIVE_CODE);
        seedStatus(PartyReferenceCodes.ENTITY_PARTY, PartyReferenceCodes.STATUS_DELETED_CODE);
        seedStatus(PartyReferenceCodes.ENTITY_PARTY_ROLE, PartyReferenceCodes.STATUS_ACTIVE_CODE);
        seedStatus(PartyReferenceCodes.ENTITY_PARTY_ROLE, PartyReferenceCodes.STATUS_DELETED_CODE);
        seedStatus(PartyReferenceCodes.ENTITY_SYSTEM_USER, PartyReferenceCodes.STATUS_ACTIVE_CODE);

        seedPartyType();
        seedPartyRoleType(PartyReferenceCodes.PARTY_ROLE_TYPE_CUSTOMER_CODE, "Müşteri");
        seedPartyRoleType(PartyReferenceCodes.PARTY_ROLE_TYPE_USER_CODE, "Kullanıcı");
    }

    @Test
    @DisplayName("Ilk istekte Party -> PartyRole(USER) -> SystemUser zinciri kurulur")
    // Zincir LAZY bağlarla gezildiği için assert'ler açık bir session içinde çalışmalı.
    @Transactional
    void provisionsPartyChainOnFirstRequest() {
        String subject = UUID.randomUUID().toString();

        systemUserService.ensureProvisioned(subject, "ayse.yilmaz");

        Optional<SystemUser> created = systemUserRepository.findByKeycloakUserId(subject);
        assertThat(created).isPresent();

        SystemUser systemUser = created.get();
        assertThat(systemUser.getUsername()).isEqualTo("ayse.yilmaz");
        assertThat(systemUser.getPartyRole().getPartyRoleType().getShortCode())
                .isEqualTo(PartyReferenceCodes.PARTY_ROLE_TYPE_USER_CODE);
        assertThat(systemUser.getPartyRole().getParty()).isNotNull();
        assertThat(systemUser.getPartyRole().getDeletedDate()).isNull();
    }

    @Test
    @DisplayName("Sonraki isteklerde kayit yeniden olusturulmaz (idempotent)")
    void doesNotDuplicateOnSubsequentRequests() {
        String subject = UUID.randomUUID().toString();

        systemUserService.ensureProvisioned(subject, "ayse.yilmaz");
        long partyCountAfterFirst = partyRepository.count();

        systemUserService.ensureProvisioned(subject, "ayse.yilmaz");
        systemUserService.ensureProvisioned(subject, "ayse.yilmaz");

        assertThat(systemUserRepository.findAll())
                .filteredOn(user -> subject.equals(user.getKeycloakUserId()))
                .hasSize(1);
        assertThat(partyRepository.count()).isEqualTo(partyCountAfterFirst);
    }

    @Test
    @DisplayName("Kimliksiz istek (sub yok) kayit olusturmaz")
    void ignoresMissingSubject() {
        long before = systemUserRepository.count();

        systemUserService.ensureProvisioned(null, "ayse.yilmaz");
        systemUserService.ensureProvisioned("  ", "ayse.yilmaz");

        assertThat(systemUserRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("Bir rol pasiflesince, party'nin diger aktif rolu varsa party pasiflesmez")
    void keepsPartyActiveWhileAnotherRoleRemains() {
        // Aynı kişi hem müşterimiz hem sistem kullanıcımız: tek party, iki rol.
        PartyRole customerRole = partyRoleService.createCustomerRoleForIndividual();
        Party party = customerRole.getParty();
        PartyRole userRole = attachRole(party, PartyReferenceCodes.PARTY_ROLE_TYPE_USER_CODE);

        partyRoleService.deactivate(customerRole);

        assertThat(partyRepository.findById(party.getId()))
                .get()
                .satisfies(reloaded -> assertThat(reloaded.getDeletedDate()).isNull());
        assertThat(partyRoleRepository.findById(userRole.getId()))
                .get()
                .satisfies(reloaded -> assertThat(reloaded.getDeletedDate()).isNull());
    }

    @Test
    @DisplayName("Son aktif rol de pasiflesince party pasiflesir")
    void deactivatesPartyWhenLastRoleIsRemoved() {
        PartyRole customerRole = partyRoleService.createCustomerRoleForIndividual();
        Party party = customerRole.getParty();
        PartyRole userRole = attachRole(party, PartyReferenceCodes.PARTY_ROLE_TYPE_USER_CODE);

        partyRoleService.deactivate(customerRole);
        partyRoleService.deactivate(userRole);

        assertThat(partyRepository.findById(party.getId()))
                .get()
                .satisfies(reloaded -> assertThat(reloaded.getDeletedDate()).isNotNull());
    }

    /** Var olan bir party'ye ikinci bir rol bağlar (manager yalnızca yeni party açtığı için elle). */
    private PartyRole attachRole(Party party, String roleTypeCode) {
        PartyRole partyRole = new PartyRole();
        partyRole.setParty(party);
        partyRole.setPartyRoleType(partyRoleTypeRepository.findByShortCode(roleTypeCode).orElseThrow());
        partyRole.setGeneralStatus(generalStatusRepository
                .findByEntityCodeNameAndShortCode(
                        PartyReferenceCodes.ENTITY_PARTY_ROLE, PartyReferenceCodes.STATUS_ACTIVE_CODE)
                .orElseThrow());
        return partyRoleRepository.save(partyRole);
    }

    private void seedStatus(String entityCodeName, String shortCode) {
        if (generalStatusRepository.findByEntityCodeNameAndShortCode(entityCodeName, shortCode).isPresent()) {
            return;
        }

        GeneralStatus status = new GeneralStatus();
        status.setName(shortCode);
        status.setShortCode(shortCode);
        status.setEntityCodeName(entityCodeName);
        status.setEntityName(entityCodeName);
        generalStatusRepository.save(status);
    }

    private void seedPartyType() {
        if (generalTypeRepository.findByEntityCodeNameAndShortCode(
                PartyReferenceCodes.ENTITY_CAM_PARTY_TYPE,
                PartyReferenceCodes.PARTY_TYPE_INDIVIDUAL_CODE).isPresent()) {
            return;
        }

        GeneralType type = new GeneralType();
        type.setName("Bireysel");
        type.setShortCode(PartyReferenceCodes.PARTY_TYPE_INDIVIDUAL_CODE);
        type.setEntityCodeName(PartyReferenceCodes.ENTITY_CAM_PARTY_TYPE);
        generalTypeRepository.save(type);
    }

    private void seedPartyRoleType(String shortCode, String name) {
        if (partyRoleTypeRepository.findByShortCode(shortCode).isPresent()) {
            return;
        }

        PartyRoleType roleType = new PartyRoleType();
        roleType.setName(name);
        roleType.setShortCode(shortCode);
        partyRoleTypeRepository.save(roleType);
    }
}
