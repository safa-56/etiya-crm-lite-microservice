package com.etiya.productservice.business.concretes;

import com.etiya.productservice.business.abstracts.OutboxService;
import com.etiya.productservice.business.abstracts.ProductService;
import com.etiya.productservice.business.constants.Messages;
import com.etiya.productservice.business.constants.ProductEvents;
import com.etiya.productservice.business.constants.ProductSagaEvents;
import com.etiya.productservice.business.dtos.events.ProductEventPayload;
import com.etiya.productservice.business.dtos.events.ProductSagaRequestedPayload;
import com.etiya.productservice.business.dtos.requests.CreateProductRequest;
import com.etiya.productservice.business.dtos.responses.PagedResponse;
import com.etiya.productservice.business.dtos.responses.ProductDetailResponse;
import com.etiya.productservice.business.dtos.responses.ProductResponse;
import com.etiya.productservice.business.mappers.ProductMapper;
import com.etiya.productservice.business.rules.CampaignBusinessRules;
import com.etiya.productservice.business.rules.ProductOfferBusinessRules;
import com.etiya.productservice.core.constants.CacheNames;
import com.etiya.productservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.productservice.dataAccess.CampaignRepository;
import com.etiya.productservice.dataAccess.ProductOfferRepository;
import com.etiya.productservice.dataAccess.ProductRepository;
import com.etiya.productservice.entities.Campaign;
import com.etiya.productservice.entities.Product;
import com.etiya.productservice.entities.ProductOffer;
import com.etiya.productservice.entities.enums.ProductStatus;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Ürün (satılmış ürün) iş mantığı (business/concretes).
 *
 * <p>Ürün satışı bir <b>choreography Saga</b> ile kesinleşir (Customer ↔ Account
 * modeliyle aynı): ürün {@code PENDING} açılır ve fatura hesabı doğrulaması için
 * {@link ProductSagaEvents#SALE_REQUESTED} olayı yayınlanır. Doğrulama otoriter
 * olarak account-service'e bırakılır; sonuç geldiğinde
 * {@link ProductSagaManager} ürünü ACTIVE/CANCELLED yapar. Olay yayını
 * {@link OutboxService} ile aynı transaction'da yapılır (ghost event yok).
 *
 * <p>Teklif/kampanya varlığı gibi ürün-lokal kurallar {@link ProductOfferBusinessRules}
 * ve {@link CampaignBusinessRules}'a delege edilir.
 */
@Service
public class ProductManager implements ProductService {

    private final ProductRepository repository;
    private final ProductOfferRepository productOfferRepository;
    private final CampaignRepository campaignRepository;
    private final ProductMapper mapper;
    private final ProductOfferBusinessRules productOfferRules;
    private final CampaignBusinessRules campaignRules;
    private final OutboxService outboxService;

    public ProductManager(ProductRepository repository,
                          ProductOfferRepository productOfferRepository,
                          CampaignRepository campaignRepository,
                          ProductMapper mapper,
                          ProductOfferBusinessRules productOfferRules,
                          CampaignBusinessRules campaignRules,
                          OutboxService outboxService) {
        this.repository = repository;
        this.productOfferRepository = productOfferRepository;
        this.campaignRepository = campaignRepository;
        this.mapper = mapper;
        this.productOfferRules = productOfferRules;
        this.campaignRules = campaignRules;
        this.outboxService = outboxService;
    }

    @Override
    @Transactional
    public ProductResponse add(CreateProductRequest request) {
        // --- ürün-lokal kurallar (senkron) ---
        productOfferRules.checkIfProductOfferExists(request.productOfferId());

        ProductOffer offer = productOfferRepository.findByIdAndIsActiveTrue(request.productOfferId())
                .orElseThrow(() -> new BusinessException(Messages.PRODUCT_OFFER_NOT_FOUND));

        Product product = new Product();
        product.setProductOffer(offer);
        product.setAccountId(request.accountId());
        product.setAddressId(request.addressId());
        product.setPriceToBePaid(request.priceToBePaid());
        product.setName(request.name() != null ? request.name() : offer.getName());
        product.setIsActive(true);

        if (request.campaignId() != null) {
            campaignRules.checkIfCampaignExists(request.campaignId());
            Campaign campaign = campaignRepository.getReferenceById(request.campaignId());
            product.setCampaign(campaign);
        }

        // --- Saga (choreography) adım 1: ürünü PENDING olarak aç ---
        // Fatura hesabı doğrulaması (var mı / ACTIVE mi) otoriter olarak
        // account-service'e bırakılır; ürün, doğrulama sonucu gelene kadar PENDING kalır.
        product.setStatus(ProductStatus.PENDING);

        Product saved = repository.save(product);

        // --- Saga adım 1 olayı: doğrulama isteği (aynı transaction — outbox) ---
        outboxService.publish(
                ProductSagaEvents.AGGREGATE_TYPE,
                String.valueOf(saved.getId()),
                ProductSagaEvents.SALE_REQUESTED,
                new ProductSagaRequestedPayload(
                        ProductSagaEvents.SALE_REQUESTED, saved.getId(), saved.getAccountId()));

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.PRODUCTS, key = "#id")
    public ProductResponse getById(Long id) {
        return mapper.toResponse(findActiveOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getAll(Pageable pageable) {
        return PagedResponse.of(repository.findAllByIsActiveTrue(pageable).map(mapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDetailResponse> getDetailsByAccount(Long accountId) {
        return repository.findAllByAccountIdAndIsActiveTrue(accountId).stream()
                .map(this::toDetail)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.PRODUCTS, key = "#id")
    public void delete(Long id) {
        Product product = findActiveOrThrow(id);
        boolean wasActive = product.getStatus() == ProductStatus.ACTIVE;

        product.setIsActive(false);
        product.setDeletedDate(LocalDateTime.now());
        repository.save(product);

        // Yalnızca satışı kesinleşmiş (ACTIVE) ürünlerde account-service sayacını
        // azaltmak için olay yayınlanır; PENDING/CANCELLED ürünlerde sayaç zaten
        // artırılmadığından olay üretilmez.
        if (wasActive) {
            outboxService.publish(
                    ProductEvents.AGGREGATE_TYPE,
                    String.valueOf(product.getId()),
                    ProductEvents.PRODUCT_DELETED,
                    new ProductEventPayload(product.getId(), product.getAccountId(), ProductEvents.PRODUCT_DELETED));
        }
    }

    // ------------------------------------------------------------------ yardımcılar

    /** FR-013 ürün detayı: teklif/özellik/kampanya bilgilerini düzleştirir. */
    private ProductDetailResponse toDetail(Product product) {
        ProductOffer offer = product.getProductOffer();
        Campaign campaign = product.getCampaign();
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                offer != null ? offer.getId() : null,
                offer != null ? offer.getName() : null,
                offer != null && offer.getProductSpec() != null ? offer.getProductSpec().getId() : null,
                offer != null && offer.getProductSpec() != null ? offer.getProductSpec().getDescription() : null,
                campaign != null ? campaign.getId() : null,
                campaign != null ? campaign.getName() : null,
                product.getAddressId(),
                product.getPriceToBePaid());
    }

    private Product findActiveOrThrow(Long id) {
        return repository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException(Messages.PRODUCT_NOT_FOUND));
    }
}
