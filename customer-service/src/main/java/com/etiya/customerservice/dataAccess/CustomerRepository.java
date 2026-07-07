package com.etiya.customerservice.dataAccess;

import com.etiya.customerservice.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Müşteri agrega kökü için repository. Alt tipler (IndividualCustomer) için
 * özel repository'ler ayrıca tanımlıdır; bu repository ortak/genel erişim içindir.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /** Aktif (silinmemiş) müşteriyi id ile getirir. */
    Optional<Customer> findByIdAndIsActiveTrue(Long id);

    /** Aktif bir müşterinin id ile var olup olmadığını kontrol eder. */
    boolean existsByIdAndIsActiveTrue(Long id);
}
