package com.etiya.customerservice.dataAccess;

import com.etiya.customerservice.entities.reference.GeneralStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Genel durum referans verisi ({@code general_status}) veri erişimi.
 *
 * <p>Arama daima <b>stabil iş kodu</b> ({@code entityCodeName} + {@code shortCode})
 * ile yapılır; surrogate id'ye koddan bağımlılık kurulmaz. Böylece seed/ETL ile
 * id'ler değişse bile iş mantığı etkilenmez.
 */
@Repository
public interface GeneralStatusRepository extends JpaRepository<GeneralStatus, Long> {

    /** Verilen entity dilimi ve kısa koda ait aktif durumu getirir. */
    Optional<GeneralStatus> findByEntityCodeNameAndShortCodeAndIsActiveTrue(
            String entityCodeName, String shortCode);
}
