package com.etiya.searchservice.dataAccess;

import com.etiya.searchservice.entities.CustomerSearchIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Müşteri arama indeksi veri erişimi.
 *
 * <p>{@link JpaSpecificationExecutor} ile FR-002'nin dinamik arama sorgusu
 * (JPA Criteria API üzerinden kurulan Specification) desteklenir. Projeksiyon
 * (upsert/remove) tarafı {@code customerId} üzerinden tekil satırı çözer.
 */
@Repository
public interface CustomerSearchIndexRepository
        extends JpaRepository<CustomerSearchIndex, Long>,
        JpaSpecificationExecutor<CustomerSearchIndex> {

    /** Müşteri (iş anahtarı) kimliğine göre indeks satırını getirir. */
    Optional<CustomerSearchIndex> findByCustomerId(Long customerId);
}
