package com.etiya.customerservice.dataAccess;

import com.etiya.customerservice.entities.PartyRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Party rolü ({@code party_roles}) veri erişimi.
 */
@Repository
public interface PartyRoleRepository extends JpaRepository<PartyRole, Long> {

    /**
     * Verilen party'nin, belirtilen rol dışındaki aktif (silinmemiş) rol sayısı.
     * Bir rol pasifleştirilirken party'nin de pasifleştirilip pasifleştirilmeyeceğine
     * karar vermek için kullanılır.
     */
    long countByPartyIdAndDeletedDateIsNullAndIdNot(Long partyId, Long partyRoleId);
}
