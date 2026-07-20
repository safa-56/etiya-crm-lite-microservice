package com.etiya.productservice.business.concretes;

import com.etiya.productservice.business.abstracts.CatalogService;
import com.etiya.productservice.business.abstracts.ProductOfferService;
import com.etiya.productservice.business.abstracts.ProductSpecService;
import com.etiya.productservice.business.abstracts.ReferenceDataService;
import com.etiya.productservice.business.constants.Messages;
import com.etiya.productservice.business.constants.ProductReferenceCodes;
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
import com.etiya.productservice.dataAccess.ProductOfferRepository;
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
    private final ProductSpecService productSpecService;
    private final CatalogService catalogService;
    private final ProductOfferMapper mapper;
    private final ProductOfferBusinessRules rules;
    private final ProductSpecBusinessRules productSpecRules;
    private final CatalogBusinessRules catalogRules;
    private final ReferenceDataService referenceDataService;

    public ProductOfferManager(ProductOfferRepository repository,
                               ProductSpecService productSpecService,
                               CatalogService catalogService,
                               ProductOfferMapper mapper,
                               ProductOfferBusinessRules rules,
                               ProductSpecBusinessRules productSpecRules,
                               CatalogBusinessRules catalogRules,
                               ReferenceDataService referenceDataService) {
        this.repository = repository;
        this.productSpecService = productSpecService;
        this.catalogService = catalogService;
        this.mapper = mapper;
        this.rules = rules;
        this.productSpecRules = productSpecRules;
        this.catalogRules = catalogRules;
        this.referenceDataService = referenceDataService;
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.PRODUCT_OFFER_LIST, allEntries = true)
    public ProductOfferResponse add(CreateProductOfferRequest request) {
        rules.checkDateRangeValid(request.startDate(), request.endDate());
        catalogRules.checkIfCatalogExists(request.catalogId());
        productSpecRules.checkIfProductSpecExists(request.productSpecId());

        Catalog catalog = catalogService.getCatalogById(request.catalogId());
        ProductSpec spec = productSpecService.getProductSpecById(request.productSpecId());

        ProductOffer offer = mapper.toEntity(request);
        offer.setCatalog(catalog);
        offer.setProductSpec(spec);
        offer.setGeneralStatus(referenceDataService.getStatus(
                ProductReferenceCodes.ENTITY_PRODUCT_OFFER, ProductReferenceCodes.STATUS_ACTIVE_CODE));

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
        return PagedResponse.of(repository.findAllByDeletedDateIsNull(pageable).map(mapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductOfferResponse> getByCatalog(Long catalogId, Pageable pageable) {
        catalogRules.checkIfCatalogExists(catalogId);
        return PagedResponse.of(
                repository.findAllByCatalogIdAndDeletedDateIsNull(catalogId, pageable).map(mapper::toResponse));
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
        offer.setGeneralStatus(referenceDataService.getStatus(
                ProductReferenceCodes.ENTITY_PRODUCT_OFFER, ProductReferenceCodes.STATUS_DELETED_CODE));
        offer.setDeletedDate(LocalDateTime.now());
        repository.save(offer);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductOffer getProductOfferById(Long id) {
        return findActiveOrThrow(id);
    }

    private ProductOffer findActiveOrThrow(Long id) {
        return repository.findByIdAndDeletedDateIsNull(id)
                .orElseThrow(() -> new BusinessException(Messages.PRODUCT_OFFER_NOT_FOUND));
    }
}
