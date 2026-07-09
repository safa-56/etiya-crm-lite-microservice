package com.etiya.productservice.business.concretes;

import com.etiya.productservice.business.abstracts.CatalogService;
import com.etiya.productservice.business.constants.Messages;
import com.etiya.productservice.business.dtos.requests.CreateCatalogRequest;
import com.etiya.productservice.business.dtos.requests.UpdateCatalogRequest;
import com.etiya.productservice.business.dtos.responses.CatalogResponse;
import com.etiya.productservice.business.dtos.responses.PagedResponse;
import com.etiya.productservice.business.mappers.CatalogMapper;
import com.etiya.productservice.core.constants.CacheNames;
import com.etiya.productservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.productservice.dataAccess.CatalogRepository;
import com.etiya.productservice.entities.Catalog;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Katalog iş mantığı (business/concretes). Soft-delete ve Redis cache uygulanır.
 */
@Service
public class CatalogManager implements CatalogService {

    private final CatalogRepository repository;
    private final CatalogMapper mapper;

    public CatalogManager(CatalogRepository repository, CatalogMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.CATALOG_LIST, allEntries = true)
    public CatalogResponse add(CreateCatalogRequest request) {
        Catalog entity = mapper.toEntity(request);
        entity.setIsActive(true);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CATALOGS, key = "#id")
    public CatalogResponse getById(Long id) {
        return mapper.toResponse(findActiveOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CATALOG_LIST,
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public PagedResponse<CatalogResponse> getAll(Pageable pageable) {
        return PagedResponse.of(repository.findAllByIsActiveTrue(pageable).map(mapper::toResponse));
    }

    @Override
    @Transactional
    @Caching(
            put = @CachePut(value = CacheNames.CATALOGS, key = "#id"),
            evict = @CacheEvict(value = CacheNames.CATALOG_LIST, allEntries = true)
    )
    public CatalogResponse update(Long id, UpdateCatalogRequest request) {
        Catalog entity = findActiveOrThrow(id);
        entity.setName(request.name());
        entity.setDescription(request.description());
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.CATALOGS, key = "#id"),
            @CacheEvict(value = CacheNames.CATALOG_LIST, allEntries = true)
    })
    public void delete(Long id) {
        Catalog entity = findActiveOrThrow(id);
        entity.setIsActive(false);
        entity.setDeletedDate(LocalDateTime.now());
        repository.save(entity);
    }

    private Catalog findActiveOrThrow(Long id) {
        return repository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException(Messages.CATALOG_NOT_FOUND));
    }
}
