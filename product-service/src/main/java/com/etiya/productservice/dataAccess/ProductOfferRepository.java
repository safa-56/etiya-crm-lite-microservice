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

    Optional<ProductOffer> findByIdAndDeletedDateIsNull(Long id);

    Page<ProductOffer> findAllByDeletedDateIsNull(Pageable pageable);

    boolean existsByIdAndDeletedDateIsNull(Long id);

    /** Bir kataloga (kategoriye) bağlı silinmemiş teklifler (Catalog sekmesi araması). */
    Page<ProductOffer> findAllByCatalogIdAndDeletedDateIsNull(Long catalogId, Pageable pageable);

    /** Verilen id'lerden silinmemiş teklifler (kampanya paketini kurarken doğrulama/okuma). */
    List<ProductOffer> findAllByIdInAndDeletedDateIsNull(List<Long> ids);
}
