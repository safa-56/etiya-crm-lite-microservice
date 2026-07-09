package com.etiya.productservice.dataAccess;

import com.etiya.productservice.entities.CatalogOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Katalog–Teklif bağı (CatalogOffer) veri erişimi.
 */
@Repository
public interface CatalogOfferRepository extends JpaRepository<CatalogOffer, Long> {

    boolean existsByCatalogIdAndProductOfferIdAndIsActiveTrue(Long catalogId, Long productOfferId);
}
