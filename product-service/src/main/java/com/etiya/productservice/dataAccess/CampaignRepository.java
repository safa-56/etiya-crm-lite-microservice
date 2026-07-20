package com.etiya.productservice.dataAccess;

import com.etiya.productservice.entities.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Kampanya (Campaign) veri erişimi. Yalnızca aktif kayıtları döner.
 */
@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    Optional<Campaign> findByIdAndDeletedDateIsNull(Long id);

    Page<Campaign> findAllByDeletedDateIsNull(Pageable pageable);

    boolean existsByIdAndDeletedDateIsNull(Long id);
}
