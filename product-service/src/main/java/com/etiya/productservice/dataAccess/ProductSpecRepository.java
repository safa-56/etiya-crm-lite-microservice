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
 * <p>Soft-delete gereği yalnızca silinmemiş ({@code deleted_date IS NULL}) kayıtlar
 * dönen türetilmiş sorgular sağlanır.
 */
@Repository
public interface ProductSpecRepository extends JpaRepository<ProductSpec, Long> {

    Optional<ProductSpec> findByIdAndDeletedDateIsNull(Long id);

    Page<ProductSpec> findAllByDeletedDateIsNull(Pageable pageable);

    boolean existsByIdAndDeletedDateIsNull(Long id);
}
