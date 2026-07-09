package com.etiya.productservice.business.concretes;

import com.etiya.productservice.business.abstracts.ProductSpecService;
import com.etiya.productservice.business.constants.Messages;
import com.etiya.productservice.business.dtos.requests.CreateProductSpecRequest;
import com.etiya.productservice.business.dtos.requests.UpdateProductSpecRequest;
import com.etiya.productservice.business.dtos.responses.PagedResponse;
import com.etiya.productservice.business.dtos.responses.ProductSpecResponse;
import com.etiya.productservice.business.mappers.ProductSpecMapper;
import com.etiya.productservice.core.constants.CacheNames;
import com.etiya.productservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.productservice.dataAccess.ProductSpecRepository;
import com.etiya.productservice.entities.ProductSpec;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Ürün teknik özelliği iş mantığı (business/concretes).
 *
 * <p>Silme fizikseldir değildir: {@code isActive=false} + {@code deletedDate}
 * (soft-delete). Listeleme sayfalıdır; okuma sonuçları Redis'te cache'lenir.
 */
@Service
public class ProductSpecManager implements ProductSpecService {

    private final ProductSpecRepository repository;
    private final ProductSpecMapper mapper;

    public ProductSpecManager(ProductSpecRepository repository, ProductSpecMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.PRODUCT_SPEC_LIST, allEntries = true)
    public ProductSpecResponse add(CreateProductSpecRequest request) {
        ProductSpec entity = mapper.toEntity(request);
        entity.setIsActive(true);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.PRODUCT_SPECS, key = "#id")
    public ProductSpecResponse getById(Long id) {
        return mapper.toResponse(findActiveOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.PRODUCT_SPEC_LIST,
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public PagedResponse<ProductSpecResponse> getAll(Pageable pageable) {
        return PagedResponse.of(repository.findAllByIsActiveTrue(pageable).map(mapper::toResponse));
    }

    @Override
    @Transactional
    @Caching(
            put = @CachePut(value = CacheNames.PRODUCT_SPECS, key = "#id"),
            evict = @CacheEvict(value = CacheNames.PRODUCT_SPEC_LIST, allEntries = true)
    )
    public ProductSpecResponse update(Long id, UpdateProductSpecRequest request) {
        ProductSpec entity = findActiveOrThrow(id);
        entity.setName(request.name());
        entity.setDescription(request.description());
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.PRODUCT_SPECS, key = "#id"),
            @CacheEvict(value = CacheNames.PRODUCT_SPEC_LIST, allEntries = true)
    })
    public void delete(Long id) {
        ProductSpec entity = findActiveOrThrow(id);
        entity.setIsActive(false);
        entity.setDeletedDate(LocalDateTime.now());
        repository.save(entity);
    }

    private ProductSpec findActiveOrThrow(Long id) {
        return repository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException(Messages.PRODUCT_SPEC_NOT_FOUND));
    }
}
