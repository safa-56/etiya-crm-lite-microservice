package com.etiya.productservice.business.abstracts;

import com.etiya.productservice.business.dtos.requests.CreateProductOfferRequest;
import com.etiya.productservice.business.dtos.requests.UpdateProductOfferRequest;
import com.etiya.productservice.business.dtos.responses.PagedResponse;
import com.etiya.productservice.business.dtos.responses.ProductOfferResponse;
import org.springframework.data.domain.Pageable;

/**
 * Ürün teklifi iş servisi (business abstraction).
 *
 * <p>CRUD'a ek olarak Teklif Seçimi ekranı (FR-014) için katalog (kategori) bazlı
 * arama ucunu tanımlar. Kampanya bazlı listeleme kampanya detayından
 * ({@code GET /campaigns/{id}}) beslenir.
 */
public interface ProductOfferService {

    ProductOfferResponse add(CreateProductOfferRequest request);

    ProductOfferResponse getById(Long id);

    PagedResponse<ProductOfferResponse> getAll(Pageable pageable);

    /** Bir kataloga (kategoriye) bağlı teklifler (Catalog sekmesi). */
    PagedResponse<ProductOfferResponse> getByCatalog(Long catalogId, Pageable pageable);

    ProductOfferResponse update(Long id, UpdateProductOfferRequest request);

    void delete(Long id);
}
