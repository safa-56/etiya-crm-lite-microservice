package com.etiya.orderservice.dataAccess;

import com.etiya.orderservice.entities.reference.GeneralType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Genel tip referans verisi ({@code general_type}) veri erişimi.
 */
@Repository
public interface GeneralTypeRepository extends JpaRepository<GeneralType, Long> {

    /** Verilen entity dilimi ve kısa koda ait aktif tipi getirir. */
    Optional<GeneralType> findByEntityCodeNameAndShortCode(
            String entityCodeName, String shortCode);
}
