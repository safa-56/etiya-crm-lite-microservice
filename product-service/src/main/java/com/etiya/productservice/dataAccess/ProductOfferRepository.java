package com.etiya.productservice.dataAccess;

import com.etiya.productservice.entities.ProductOffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Ürün teklifi (ProductOffer) veri erişimi.
 *
 * <p>Teklif Seçimi ekranı (FR-014) için katalog ve kampanya bazlı arama sorguları
 * sağlar. Yalnızca aktif kayıtları döner.
 */
@Repository
public interface ProductOfferRepository extends JpaRepository<ProductOffer, Long> {

    Optional<ProductOffer> findByIdAndIsActiveTrue(Long id);

    Page<ProductOffer> findAllByIsActiveTrue(Pageable pageable);

    boolean existsByIdAndIsActiveTrue(Long id);

    /** Bir kataloga bağlı aktif teklifler (Catalog sekmesi araması). */
    @Query("""
            select o from ProductOffer o
            join CatalogOffer co on co.productOffer = o
            where co.catalog.id = :catalogId
              and o.isActive = true
              and co.isActive = true
            """)
    Page<ProductOffer> findAllByCatalogId(@Param("catalogId") Long catalogId, Pageable pageable);

    /** Bir kampanyaya bağlı aktif teklifler (Campaign sekmesi araması). */
    @Query("""
            select o from ProductOffer o
            join CampaignOffer co on co.productOffer = o
            where co.campaign.id = :campaignId
              and o.isActive = true
              and co.isActive = true
            """)
    Page<ProductOffer> findAllByCampaignId(@Param("campaignId") Long campaignId, Pageable pageable);
}
