package com.etiya.productservice.dataAccess;

import com.etiya.productservice.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Ürün (Product) veri erişimi — satılmış ürün kayıtları.
 *
 * <p>FR-013 (Fatura Hesabına Bağlı Ürün Detayları) için fatura hesabına göre
 * listeleme sağlar. Yalnızca aktif kayıtları döner.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndDeletedDateIsNull(Long id);

    Page<Product> findAllByDeletedDateIsNull(Pageable pageable);

    /** Bir fatura hesabına bağlı silinmemiş ürünler (FR-013 ürün detay tablosu). */
    List<Product> findAllByAccountIdAndDeletedDateIsNull(Long accountId);
}
