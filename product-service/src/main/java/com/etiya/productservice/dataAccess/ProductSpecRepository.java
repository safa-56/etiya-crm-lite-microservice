package com.etiya.productservice.dataAccess;

import com.etiya.productservice.entities.ProductSpec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Ürün teknik özelliği (ProductSpec) veri erişimi.
 *
 * <p>Soft-delete gereği yalnızca aktif ({@code is_active = true}) kayıtlar dönen
 * türetilmiş sorgular sağlanır.
 */
@Repository
public interface ProductSpecRepository extends JpaRepository<ProductSpec, Long> {

    Optional<ProductSpec> findByIdAndIsActiveTrue(Long id);

    Page<ProductSpec> findAllByIsActiveTrue(Pageable pageable);

    boolean existsByIdAndIsActiveTrue(Long id);
}
