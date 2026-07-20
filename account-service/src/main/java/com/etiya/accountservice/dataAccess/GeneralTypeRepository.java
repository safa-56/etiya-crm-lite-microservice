package com.etiya.accountservice.dataAccess;

import com.etiya.accountservice.entities.reference.GeneralType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Genel tip referans verisi ({@code general_type}) veri erişimi.
 */
@Repository
public interface GeneralTypeRepository extends JpaRepository<GeneralType, Long> {

    /** Verilen entity dilimi ve kısa koda ait tipi getirir. */
    Optional<GeneralType> findByEntityCodeNameAndShortCode(
            String entityCodeName, String shortCode);
}
