package com.etiya.productservice.dataAccess;

import com.etiya.productservice.entities.Catalog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Katalog (Catalog) veri erişimi. Yalnızca aktif kayıtları döner.
 */
@Repository
public interface CatalogRepository extends JpaRepository<Catalog, Long> {

    Optional<Catalog> findByIdAndIsActiveTrue(Long id);

    Page<Catalog> findAllByIsActiveTrue(Pageable pageable);

    boolean existsByIdAndIsActiveTrue(Long id);
}
