package com.etiya.searchservice.business.abstracts;

import com.etiya.searchservice.business.dtos.requests.CustomerSearchRequest;
import com.etiya.searchservice.business.dtos.responses.CustomerSearchResponse;
import com.etiya.searchservice.business.dtos.responses.PagedResponse;
import org.springframework.data.domain.Pageable;

/**
 * Müşteri arama (sorgu tarafı) servisi — FR-002.
 */
public interface CustomerSearchService {

    /**
     * Verilen kriterlere göre müşteri arama indeksini sorgular (dinamik
     * Specification). Sonuç sayfalıdır (ilk sayfa varsayılan 50 kayıt — ACC-19).
     */
    PagedResponse<CustomerSearchResponse> search(CustomerSearchRequest request, Pageable pageable);
}
