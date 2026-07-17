package com.etiya.customerservice.dataAccess;

import com.etiya.customerservice.entities.reference.GeneralType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Genel tip referans verisi ({@code general_type}) veri erişimi.
 *
 * <p>Arama daima stabil iş kodu ({@code entityCodeName} + {@code shortCode}) iledir.
 */
@Repository
public interface GeneralTypeRepository extends JpaRepository<GeneralType, Long> {

    /** Verilen entity dilimi ve kısa koda ait aktif tipi getirir. */
    Optional<GeneralType> findByEntityCodeNameAndShortCodeAndIsActiveTrue(
            String entityCodeName, String shortCode);
}
