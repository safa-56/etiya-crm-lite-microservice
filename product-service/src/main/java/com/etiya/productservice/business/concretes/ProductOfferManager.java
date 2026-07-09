package com.etiya.productservice.business.concretes;

import com.etiya.productservice.business.abstracts.ProductOfferService;
import com.etiya.productservice.business.constants.Messages;
import com.etiya.productservice.business.dtos.requests.CreateProductOfferRequest;
import com.etiya.productservice.business.dtos.requests.UpdateProductOfferRequest;
import com.etiya.productservice.business.dtos.responses.PagedResponse;
import com.etiya.productservice.business.dtos.responses.ProductOfferResponse;
import com.etiya.productservice.business.mappers.ProductOfferMapper;
import com.etiya.productservice.business.rules.CatalogBusinessRules;
import com.etiya.productservice.business.rules.ProductOfferBusinessRules;
import com.etiya.productservice.business.rules.ProductSpecBusinessRules;
import com.etiya.productservice.core.constants.CacheNames;
import com.etiya.productservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.productservice.dataAccess.CatalogRepository;
import com.etiya.productservice.dataAccess.ProductOfferRepository;
import com.etiya.productservice.dataAccess.ProductSpecRepository;
import com.etiya.productservice.entities.Catalog;
import com.etiya.productservice.entities.ProductOffer;
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
 * Ürün teklifi iş mantığı (business/concretes).
 *
 * <p>Her teklif <b>zorunlu olarak</b> bir kataloga (kategori) ve bir teknik
 * özelliğe bağlanır; iş kurallarını (tarih aralığı, katalog/özellik varlığı)
 * ilgili {@code *BusinessRules} sınıflarına delege eder. Kampanya üyeliği burada
 * değil, kampanya tarafında yönetilir. Silme soft-delete'tir; okuma sonuçları
 * Redis'te cache'lenir.
 */
@Service
public class ProductOfferManager implements ProductOfferService {

    private final ProductOfferRepository repository;
    private final ProductSpecRepository productSpecRepository;
    private final CatalogRepository catalogRepository;
    private final ProductOfferMapper mapper;
    private final ProductOfferBusinessRules rules;
    private final ProductSpecBusinessRules productSpecRules;
    private final CatalogBusinessRules catalogRules;

    public ProductOfferManager(ProductOfferRepository repository,
                               ProductSpecRepository productSpecRepository,
                               CatalogRepository catalogRepository,
                               ProductOfferMapper mapper,
                               ProductOfferBusinessRules rules,
                               ProductSpecBusinessRules productSpecRules,
                               CatalogBusinessRules catalogRules) {
        this.repository = repository;
        this.productSpecRepository = productSpecRepository;
        this.catalogRepository = catalogRepository;
        this.mapper = mapper;
        this.rules = rules;
        this.productSpecRules = productSpecRules;
        this.catalogRules = catalogRules;
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.PRODUCT_OFFER_LIST, allEntries = true)
    public ProductOfferResponse add(CreateProductOfferRequest request) {
        rules.checkDateRangeValid(request.startDate(), request.endDate());
        catalogRules.checkIfCatalogExists(request.catalogId());
        productSpecRules.checkIfProductSpecExists(request.productSpecId());

        Catalog catalog = catalogRepository.findByIdAndIsActiveTrue(request.catalogId())
                .orElseThrow(() -> new BusinessException(Messages.CATALOG_NOT_FOUND));
        ProductSpec spec = productSpecRepository.findByIdAndIsActiveTrue(request.productSpecId())
                .orElseThrow(() -> new BusinessException(Messages.PRODUCT_SPEC_NOT_FOUND));

        ProductOffer offer = mapper.toEntity(request);
        offer.setCatalog(catalog);
        offer.setProductSpec(spec);
        offer.setIsActive(true);

        return mapper.toResponse(repository.save(offer));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.PRODUCT_OFFERS, key = "#id")
    public ProductOfferResponse getById(Long id) {
        return mapper.toResponse(findActiveOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.PRODUCT_OFFER_LIST,
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public PagedResponse<ProductOfferResponse> getAll(Pageable pageable) {
        return PagedResponse.of(repository.findAllByIsActiveTrue(pageable).map(mapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductOfferResponse> getByCatalog(Long catalogId, Pageable pageable) {
        catalogRules.checkIfCatalogExists(catalogId);
        return PagedResponse.of(
                repository.findAllByCatalogIdAndIsActiveTrue(catalogId, pageable).map(mapper::toResponse));
    }

    @Override
    @Transactional
    @Caching(
            put = @CachePut(value = CacheNames.PRODUCT_OFFERS, key = "#id"),
            evict = @CacheEvict(value = CacheNames.PRODUCT_OFFER_LIST, allEntries = true)
    )
    public ProductOfferResponse update(Long id, UpdateProductOfferRequest request) {
        rules.checkDateRangeValid(request.startDate(), request.endDate());
        ProductOffer offer = findActiveOrThrow(id);
        offer.setName(request.name());
        offer.setPrice(request.price());
        offer.setStartDate(request.startDate());
        offer.setEndDate(request.endDate());
        return mapper.toResponse(repository.save(offer));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.PRODUCT_OFFERS, key = "#id"),
            @CacheEvict(value = CacheNames.PRODUCT_OFFER_LIST, allEntries = true)
    })
    public void delete(Long id) {
        ProductOffer offer = findActiveOrThrow(id);
        offer.setIsActive(false);
        offer.setDeletedDate(LocalDateTime.now());
        repository.save(offer);
    }

    private ProductOffer findActiveOrThrow(Long id) {
        return repository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException(Messages.PRODUCT_OFFER_NOT_FOUND));
    }
}
