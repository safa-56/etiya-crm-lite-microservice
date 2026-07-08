package com.etiya.accountservice.dataAccess;

import com.etiya.accountservice.entities.projection.CustomerProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Yerel müşteri projeksiyonu (read-model) veri erişimi. Kafka olaylarıyla
 * beslenir; fatura hesabı kuralları "müşteri var mı?" kontrolünü buradan yapar.
 */
@Repository
public interface CustomerProjectionRepository extends JpaRepository<CustomerProjection, Long> {

    /** Verilen müşteri, projeksiyonda aktif olarak var mı? */
    boolean existsByCustomerIdAndIsActiveTrue(Long customerId);
}
