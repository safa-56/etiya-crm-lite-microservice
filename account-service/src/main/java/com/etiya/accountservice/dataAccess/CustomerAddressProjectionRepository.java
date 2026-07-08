package com.etiya.accountservice.dataAccess;

import com.etiya.accountservice.entities.projection.CustomerAddressProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Yerel müşteri-adres projeksiyonu (read-model) veri erişimi. Kafka olaylarıyla
 * beslenir; fatura hesabı oluşturmada seçilen adresin müşteriye ait olduğunu
 * doğrulamak ve adres metnini çözmek için kullanılır.
 */
@Repository
public interface CustomerAddressProjectionRepository extends JpaRepository<CustomerAddressProjection, Long> {

    /** Bir müşterinin projeksiyondaki tüm adreslerini siler (olayda tam küme yeniden yazılır). */
    void deleteByCustomerId(Long customerId);

    /** Verilen adres, verilen müşteriye ait mi? (Tutarlılık + adres çözümleme.) */
    Optional<CustomerAddressProjection> findByAddressIdAndCustomerId(Long addressId, Long customerId);
}
