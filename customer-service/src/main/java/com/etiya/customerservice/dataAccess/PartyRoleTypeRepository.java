package com.etiya.customerservice.dataAccess;

import com.etiya.customerservice.entities.reference.PartyRoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Party rol tipi referans verisi ({@code party_role_types}) veri erişimi.
 */
@Repository
public interface PartyRoleTypeRepository extends JpaRepository<PartyRoleType, Long> {

    /** Verilen kısa koda ({@code SHRT_CODE}) ait aktif rol tipini getirir. */
    Optional<PartyRoleType> findByShortCodeAndIsActiveTrue(String shortCode);
}
