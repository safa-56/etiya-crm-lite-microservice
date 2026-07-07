package com.etiya.customerservice.dataAccess;

import com.etiya.customerservice.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adres veri erişimi.
 *
 * <p>Soft-delete gereği yalnızca aktif ({@code is_active = true}) kayıtlar
 * dönen türetilmiş sorgular sağlanır.
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /** Aktif (silinmemiş) adresi id ile getirir. */
    Optional<Address> findByIdAndIsActiveTrue(Long id);

    /** Tüm aktif adresleri getirir. */
    List<Address> findAllByIsActiveTrue();

    /** Aktif bir adresin id ile var olup olmadığını kontrol eder. */
    boolean existsByIdAndIsActiveTrue(Long id);
}
