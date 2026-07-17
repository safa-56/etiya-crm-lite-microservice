package com.etiya.customerservice.dataAccess;

import com.etiya.customerservice.entities.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Party ({@code parties}) veri erişimi.
 */
@Repository
public interface PartyRepository extends JpaRepository<Party, Long> {
}
