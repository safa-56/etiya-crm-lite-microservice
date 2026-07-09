package com.etiya.productservice.business.abstracts;

import com.etiya.productservice.business.dtos.requests.CreateProductRequest;
import com.etiya.productservice.business.dtos.responses.PagedResponse;
import com.etiya.productservice.business.dtos.responses.ProductDetailResponse;
import com.etiya.productservice.business.dtos.responses.ProductResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Ürün (satılmış ürün) iş servisi (business abstraction).
 *
 * <p>Ürün oluşturma/silme, account-service ile Outbox+Debezium üzerinden olay
 * yayınlar (ProductCreated/ProductDeleted). FR-013 için fatura hesabına bağlı
 * ürün detaylarını salt okunur listeler.
 */
public interface ProductService {

    ProductResponse add(CreateProductRequest request);

    ProductResponse getById(Long id);

    PagedResponse<ProductResponse> getAll(Pageable pageable);

    /** Fatura hesabına bağlı ürün detayları (FR-013, salt okunur). */
    List<ProductDetailResponse> getDetailsByAccount(Long accountId);

    void delete(Long id);
}
