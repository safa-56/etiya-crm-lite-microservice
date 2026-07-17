package com.etiya.customerservice.dataAccess;

import com.etiya.customerservice.entities.PartyRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Party rolü ({@code party_roles}) veri erişimi.
 */
@Repository
public interface PartyRoleRepository extends JpaRepository<PartyRole, Long> {
}
