package com.etiya.productservice.business.abstracts;

import com.etiya.productservice.business.dtos.requests.CreateCatalogRequest;
import com.etiya.productservice.business.dtos.requests.UpdateCatalogRequest;
import com.etiya.productservice.business.dtos.responses.CatalogResponse;
import com.etiya.productservice.business.dtos.responses.PagedResponse;
import com.etiya.productservice.entities.Catalog;
import org.springframework.data.domain.Pageable;

/**
 * Katalog iş servisi (business abstraction). CRUD işlemlerini tanımlar.
 */
public interface CatalogService {

    CatalogResponse add(CreateCatalogRequest request);

    CatalogResponse getById(Long id);

    /** İlişki kurulumu için aktif katalog entity'sini döner; yoksa iş hatası fırlatır. */
    Catalog getCatalogById(Long id);

    PagedResponse<CatalogResponse> getAll(Pageable pageable);

    CatalogResponse update(Long id, UpdateCatalogRequest request);

    void delete(Long id);
}
