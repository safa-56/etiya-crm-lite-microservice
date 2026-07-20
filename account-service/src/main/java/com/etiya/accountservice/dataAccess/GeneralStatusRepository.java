package com.etiya.accountservice.dataAccess;

import com.etiya.accountservice.entities.reference.GeneralStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Genel durum referans verisi ({@code general_status}) veri erişimi.
 *
 * <p>Arama daima <b>stabil iş kodu</b> ({@code entityCodeName} + {@code shortCode})
 * ile yapılır; surrogate id'ye koddan bağımlılık kurulmaz.
 */
@Repository
public interface GeneralStatusRepository extends JpaRepository<GeneralStatus, Long> {

    /** Verilen entity dilimi ve kısa koda ait aktif durumu getirir. */
    Optional<GeneralStatus> findByEntityCodeNameAndShortCode(
            String entityCodeName, String shortCode);
}
