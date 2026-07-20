package com.etiya.cartservice.dataAccess;

import com.etiya.cartservice.entities.reference.GeneralStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Genel durum referans verisi ({@code general_status}) veri erişimi.
 *
 * <p>Arama daima stabil iş kodu ({@code entityCodeName} + {@code shortCode}) iledir.
 */
@Repository
public interface GeneralStatusRepository extends JpaRepository<GeneralStatus, Long> {

    /** Verilen entity dilimi ve kısa koda ait durumu getirir. */
    Optional<GeneralStatus> findByEntityCodeNameAndShortCode(String entityCodeName, String shortCode);
}
