package com.etiya.productservice.dataAccess;

import com.etiya.productservice.entities.ProductOffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Ürün teklifi (ProductOffer) veri erişimi.
 *
 * <p>Teklif Seçimi ekranı (FR-014) için katalog (kategori) bazlı arama sağlar.
 * Yalnızca aktif kayıtları döner.
 */
@Repository
public interface ProductOfferRepository extends JpaRepository<ProductOffer, Long> {

    Optional<ProductOffer> findByIdAndIsActiveTrue(Long id);

    Page<ProductOffer> findAllByIsActiveTrue(Pageable pageable);

    boolean existsByIdAndIsActiveTrue(Long id);

    /** Bir kataloga (kategoriye) bağlı aktif teklifler (Catalog sekmesi araması). */
    Page<ProductOffer> findAllByCatalogIdAndIsActiveTrue(Long catalogId, Pageable pageable);

    /** Verilen id'lerden aktif olan teklifler (kampanya paketini kurarken doğrulama/okuma). */
    List<ProductOffer> findAllByIdInAndIsActiveTrue(List<Long> ids);
}
