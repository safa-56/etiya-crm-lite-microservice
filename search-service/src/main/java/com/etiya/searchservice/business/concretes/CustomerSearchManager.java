package com.etiya.searchservice.business.concretes;

import com.etiya.searchservice.business.abstracts.CustomerSearchService;
import com.etiya.searchservice.business.dtos.requests.CustomerSearchRequest;
import com.etiya.searchservice.business.dtos.responses.CustomerSearchResponse;
import com.etiya.searchservice.business.dtos.responses.PagedResponse;
import com.etiya.searchservice.business.mappers.CustomerSearchMapper;
import com.etiya.searchservice.business.rules.CustomerSearchBusinessRules;
import com.etiya.searchservice.business.specifications.CustomerSearchSpecification;
import com.etiya.searchservice.core.constants.CacheNames;
import com.etiya.searchservice.dataAccess.CustomerSearchIndexRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Müşteri arama (sorgu tarafı) iş mantığı — FR-002.
 *
 * <p>Akış: First/Last Name trim (ACC-10) → format doğrulaması (ACC-04..09) →
 * dinamik Specification (ACC-14..17) → sayfalı sonuç (ACC-19/20). Sonuç, sorgu
 * parametrelerini içeren bir anahtarla Redis'te kısa süreli cache'lenir.
 */
@Service
public class CustomerSearchManager implements CustomerSearchService {

    private final CustomerSearchIndexRepository repository;
    private final CustomerSearchMapper mapper;
    private final CustomerSearchBusinessRules rules;

    public CustomerSearchManager(CustomerSearchIndexRepository repository,
                                 CustomerSearchMapper mapper,
                                 CustomerSearchBusinessRules rules) {
        this.repository = repository;
        this.mapper = mapper;
        this.rules = rules;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CUSTOMER_SEARCH,
            key = "#request.cacheKey() + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public PagedResponse<CustomerSearchResponse> search(CustomerSearchRequest request, Pageable pageable) {
        // ACC-10: First/Last Name baş/son boşluklarını temizle, sonra doğrula.
        CustomerSearchRequest normalized = request.withTrimmedNames();
        rules.validate(normalized);

        return PagedResponse.of(
                repository.findAll(CustomerSearchSpecification.build(normalized), pageable)
                        .map(mapper::toResponse));
    }
}
