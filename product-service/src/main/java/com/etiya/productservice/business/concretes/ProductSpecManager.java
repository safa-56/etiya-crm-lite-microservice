package com.etiya.productservice.business.concretes;

import com.etiya.productservice.business.abstracts.ProductSpecService;
import com.etiya.productservice.business.abstracts.ReferenceDataService;
import com.etiya.productservice.business.constants.Messages;
import com.etiya.productservice.business.constants.ProductReferenceCodes;
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
 * <p>Silme fiziksel değildir: durum {@code general_status}'ta DEL'e çekilir +
 * {@code deletedDate} yazılır (soft-delete). Listeleme sayfalıdır; okuma sonuçları
 * Redis'te cache'lenir.
 */
@Service
public class ProductSpecManager implements ProductSpecService {

    private final ProductSpecRepository repository;
    private final ProductSpecMapper mapper;
    private final ReferenceDataService referenceDataService;

    public ProductSpecManager(ProductSpecRepository repository, ProductSpecMapper mapper,
                              ReferenceDataService referenceDataService) {
        this.repository = repository;
        this.mapper = mapper;
        this.referenceDataService = referenceDataService;
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.PRODUCT_SPEC_LIST, allEntries = true)
    public ProductSpecResponse add(CreateProductSpecRequest request) {
        ProductSpec entity = mapper.toEntity(request);
        entity.setGeneralStatus(referenceDataService.getStatus(
                ProductReferenceCodes.ENTITY_PRODUCT_SPEC, ProductReferenceCodes.STATUS_ACTIVE_CODE));
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
        return PagedResponse.of(repository.findAllByDeletedDateIsNull(pageable).map(mapper::toResponse));
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
        entity.setGeneralStatus(referenceDataService.getStatus(
                ProductReferenceCodes.ENTITY_PRODUCT_SPEC, ProductReferenceCodes.STATUS_DELETED_CODE));
        entity.setDeletedDate(LocalDateTime.now());
        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductSpec getProductSpecById(Long id) {
        return findActiveOrThrow(id);
    }

    private ProductSpec findActiveOrThrow(Long id) {
        return repository.findByIdAndDeletedDateIsNull(id)
                .orElseThrow(() -> new BusinessException(Messages.PRODUCT_SPEC_NOT_FOUND));
    }
}
