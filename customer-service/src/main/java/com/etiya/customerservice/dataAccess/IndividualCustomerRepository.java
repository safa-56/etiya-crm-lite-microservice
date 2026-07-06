package com.etiya.customerservice.dataAccess;

import com.etiya.customerservice.entities.IndividualCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Bireysel müşteri (IndividualCustomer) veri erişimi.
 *
 * <p>Soft-delete gereği yalnızca aktif ({@code is_active = true}) kayıtlar
 * dönen türetilmiş sorgular sağlanır.
 */
@Repository
public interface IndividualCustomerRepository extends JpaRepository<IndividualCustomer, Long> {

    /** Aktif (silinmemiş) bireysel müşteriyi id ile getirir. */
    Optional<IndividualCustomer> findByIdAndIsActiveTrue(Long id);

    /** Tüm aktif bireysel müşterileri getirir. */
    List<IndividualCustomer> findAllByIsActiveTrue();

    /** Aktif bir bireysel müşterinin id ile var olup olmadığını kontrol eder. */
    boolean existsByIdAndIsActiveTrue(Long id);
}
