package com.etiya.customerservice.dataAccess;

import com.etiya.customerservice.entities.CustomerContactInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Müşteri iletişim bilgisi veri erişimi.
 *
 * <p>Soft-delete gereği yalnızca aktif ({@code is_active = true}) kayıtlar
 * dönen türetilmiş sorgular sağlanır.
 */
@Repository
public interface CustomerContactInfoRepository extends JpaRepository<CustomerContactInfo, Long> {

    /** Verilen e-postanın aktif bir kayıtta zaten kullanılıp kullanılmadığını kontrol eder. */
    boolean existsByEmailIgnoreCaseAndIsActiveTrue(String email);

    /** Aktif (silinmemiş) iletişim bilgisini id ile getirir. */
    Optional<CustomerContactInfo> findByIdAndIsActiveTrue(Long id);

    /** Tüm aktif iletişim bilgilerini getirir. */
    List<CustomerContactInfo> findAllByIsActiveTrue();

    /** Aktif bir iletişim bilgisinin id ile var olup olmadığını kontrol eder. */
    boolean existsByIdAndIsActiveTrue(Long id);
}
