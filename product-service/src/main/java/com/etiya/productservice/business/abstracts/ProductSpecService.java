package com.etiya.productservice.business.abstracts;

import com.etiya.productservice.business.dtos.requests.CreateProductSpecRequest;
import com.etiya.productservice.business.dtos.requests.UpdateProductSpecRequest;
import com.etiya.productservice.business.dtos.responses.PagedResponse;
import com.etiya.productservice.business.dtos.responses.ProductSpecResponse;
import org.springframework.data.domain.Pageable;

/**
 * Ürün teknik özelliği iş servisi (business abstraction). CRUD işlemlerini tanımlar.
 */
public interface ProductSpecService {

    ProductSpecResponse add(CreateProductSpecRequest request);

    ProductSpecResponse getById(Long id);

    PagedResponse<ProductSpecResponse> getAll(Pageable pageable);

    ProductSpecResponse update(Long id, UpdateProductSpecRequest request);

    void delete(Long id);
}
