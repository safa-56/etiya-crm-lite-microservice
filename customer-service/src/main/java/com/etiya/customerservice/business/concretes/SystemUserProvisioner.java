package com.etiya.customerservice.business.concretes;

import com.etiya.customerservice.business.abstracts.PartyRoleService;
import com.etiya.customerservice.business.abstracts.ReferenceDataService;
import com.etiya.customerservice.business.constants.PartyReferenceCodes;
import com.etiya.customerservice.dataAccess.SystemUserRepository;
import com.etiya.customerservice.entities.PartyRole;
import com.etiya.customerservice.entities.SystemUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sistem kullanıcısının party zincirini oluşturan yazma yolu.
 *
 * <p>{@link SystemUserManager}'dan ayrı bir bean olarak durur çünkü okuma yolu
 * (bilinen kullanıcı) transaction açmadan çalışmalıdır. Aynı sınıf içinde
 * {@code @Transactional} bir metoda self-invocation yapılsaydı Spring proxy'si
 * devreye girmez ve <b>party, rol ve kullanıcı ayrı ayrı commit edilirdi</b>;
 * araya bir hata girdiğinde öksüz party kayıtları kalırdı.
 */
@Service
public class SystemUserProvisioner {

    private final SystemUserRepository systemUserRepository;
    private final PartyRoleService partyRoleService;
    private final ReferenceDataService referenceDataService;

    public SystemUserProvisioner(SystemUserRepository systemUserRepository,
                                 PartyRoleService partyRoleService,
                                 ReferenceDataService referenceDataService) {
        this.systemUserRepository = systemUserRepository;
        this.partyRoleService = partyRoleService;
        this.referenceDataService = referenceDataService;
    }

    /**
     * {@code Party → PartyRole(USER) → SystemUser} zincirini <b>tek transaction'da</b>
     * oluşturur. Zincirin tamamı ya yazılır ya da hiçbiri yazılmaz.
     */
    @Transactional
    public SystemUser provision(String keycloakUserId, String username) {
        PartyRole partyRole = partyRoleService.createUserRoleForIndividual();

        SystemUser systemUser = new SystemUser();
        systemUser.setPartyRole(partyRole);
        systemUser.setKeycloakUserId(keycloakUserId);
        systemUser.setUsername(username);
        systemUser.setGeneralStatus(referenceDataService.getStatus(
                PartyReferenceCodes.ENTITY_SYSTEM_USER, PartyReferenceCodes.STATUS_ACTIVE_CODE));

        return systemUserRepository.save(systemUser);
    }
}
