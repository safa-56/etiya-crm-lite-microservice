package com.etiya.customerservice.dataAccess;

import com.etiya.customerservice.entities.CustomerContactInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Müşteri iletişim bilgisi veri erişimi.
 */
@Repository
public interface CustomerContactInfoRepository extends JpaRepository<CustomerContactInfo, Long> {

    /** Verilen e-postanın aktif bir kayıtta zaten kullanılıp kullanılmadığını kontrol eder. */
    boolean existsByEmailIgnoreCaseAndIsActiveTrue(String email);
}
