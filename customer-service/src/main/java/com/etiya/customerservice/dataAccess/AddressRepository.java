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

    /**
     * Bir müşteriye ait tüm aktif adresleri getirir. Standalone adres
     * değişikliğinden sonra, müşterinin güncel adres kümesini olay olarak
     * yayınlamak (account-service projeksiyonunu tazelemek) için kullanılır.
     */
    List<Address> findByCustomer_IdAndIsActiveTrue(Long customerId);

    /** Aktif bir adresin id ile var olup olmadığını kontrol eder. */
    boolean existsByIdAndIsActiveTrue(Long id);

    /**
     * Bir müşteriye ait, şu an birincil (isPrimary=true) olan tüm aktif adresleri
     * getirir. "Bir müşterinin en fazla bir birincil adresi olur" değişmezini
     * korumak için, yeni bir adres birincil yapıldığında mevcut birincilleri
     * düşürmede kullanılır (FR-006 ACC-07).
     */
    List<Address> findByCustomer_IdAndIsPrimaryTrueAndIsActiveTrue(Long customerId);
}
