package com.etiya.productservice.business.concretes;

import com.etiya.productservice.business.abstracts.CampaignService;
import com.etiya.productservice.business.abstracts.ReferenceDataService;
import com.etiya.productservice.business.constants.Messages;
import com.etiya.productservice.business.constants.ProductReferenceCodes;
import com.etiya.productservice.business.dtos.requests.CreateCampaignRequest;
import com.etiya.productservice.business.dtos.requests.UpdateCampaignRequest;
import com.etiya.productservice.business.dtos.responses.CampaignOfferLine;
import com.etiya.productservice.business.dtos.responses.CampaignResponse;
import com.etiya.productservice.business.dtos.responses.PagedResponse;
import com.etiya.productservice.business.mappers.CampaignMapper;
import com.etiya.productservice.business.rules.CampaignBusinessRules;
import com.etiya.productservice.core.constants.CacheNames;
import com.etiya.productservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.productservice.dataAccess.CampaignOfferRepository;
import com.etiya.productservice.dataAccess.CampaignRepository;
import com.etiya.productservice.dataAccess.ProductOfferRepository;
import com.etiya.productservice.entities.Campaign;
import com.etiya.productservice.entities.CampaignOffer;
import com.etiya.productservice.entities.ProductOffer;
import com.etiya.productservice.entities.reference.GeneralStatus;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Kampanya (paket) iş mantığı (business/concretes).
 *
 * <p>Kampanya birden çok ürün teklifini tek paket fiyatıyla birleştirir. Oluşturma/
 * güncellemede paket içeriği ({@code offerIds}) doğrulanıp {@link CampaignOffer}
 * bağları kurulur; yanıt, paketin teklifleri ile liste fiyatı toplamı ve indirim
 * tutarını (savings) türeterek döner. Soft-delete ve Redis cache uygulanır.
 */
@Service
public class CampaignManager implements CampaignService {

    private final CampaignRepository repository;
    private final CampaignOfferRepository campaignOfferRepository;
    private final ProductOfferRepository productOfferRepository;
    private final CampaignMapper mapper;
    private final CampaignBusinessRules rules;
    private final ReferenceDataService referenceDataService;

    public CampaignManager(CampaignRepository repository,
                           CampaignOfferRepository campaignOfferRepository,
                           ProductOfferRepository productOfferRepository,
                           CampaignMapper mapper,
                           CampaignBusinessRules rules,
                           ReferenceDataService referenceDataService) {
        this.repository = repository;
        this.campaignOfferRepository = campaignOfferRepository;
        this.productOfferRepository = productOfferRepository;
        this.mapper = mapper;
        this.rules = rules;
        this.referenceDataService = referenceDataService;
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.CAMPAIGN_LIST, allEntries = true)
    public CampaignResponse add(CreateCampaignRequest request) {
        rules.checkOffersValidForCampaign(request.offerIds());

        Campaign entity = mapper.toEntity(request);
        entity.setGeneralStatus(referenceDataService.getStatus(
                ProductReferenceCodes.ENTITY_CAMPAIGN, ProductReferenceCodes.STATUS_ACTIVE_CODE));
        Campaign saved = repository.save(entity);

        linkOffers(saved, request.offerIds().stream().distinct().toList());
        return buildResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CAMPAIGNS, key = "#id")
    public CampaignResponse getById(Long id) {
        return buildResponse(findActiveOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CAMPAIGN_LIST,
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public PagedResponse<CampaignResponse> getAll(Pageable pageable) {
        Page<CampaignResponse> page = repository.findAllByDeletedDateIsNull(pageable).map(this::buildResponse);
        return PagedResponse.of(page);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.CAMPAIGNS, key = "#id"),
            @CacheEvict(value = CacheNames.CAMPAIGN_LIST, allEntries = true)
    })
    public CampaignResponse update(Long id, UpdateCampaignRequest request) {
        rules.checkOffersValidForCampaign(request.offerIds());

        Campaign entity = findActiveOrThrow(id);
        entity.setName(request.name());
        entity.setCampaignPrice(request.campaignPrice());
        repository.save(entity);

        // Paket içeriğini tamamen yenile: mevcut bağları pasifleştir, yenilerini kur.
        deactivateLinks(entity.getId());
        linkOffers(entity, request.offerIds().stream().distinct().toList());

        return buildResponse(entity);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.CAMPAIGNS, key = "#id"),
            @CacheEvict(value = CacheNames.CAMPAIGN_LIST, allEntries = true)
    })
    public void delete(Long id) {
        Campaign entity = findActiveOrThrow(id);
        entity.setGeneralStatus(referenceDataService.getStatus(
                ProductReferenceCodes.ENTITY_CAMPAIGN, ProductReferenceCodes.STATUS_DELETED_CODE));
        entity.setDeletedDate(LocalDateTime.now());
        repository.save(entity);
        deactivateLinks(entity.getId());
    }

    // ------------------------------------------------------------------ yardımcılar

    /** Verilen tekliflere kampanya bağlarını (CampaignOffer) kurar. */
    private void linkOffers(Campaign campaign, List<Long> offerIds) {
        GeneralStatus activeStatus = referenceDataService.getStatus(
                ProductReferenceCodes.ENTITY_CAMPAIGN_OFFER, ProductReferenceCodes.STATUS_ACTIVE_CODE);
        for (Long offerId : offerIds) {
            CampaignOffer link = new CampaignOffer();
            link.setCampaign(campaign);
            link.setProductOffer(productOfferRepository.getReferenceById(offerId));
            link.setGeneralStatus(activeStatus);
            campaignOfferRepository.save(link);
        }
    }

    /** Kampanyanın tüm aktif bağlarını pasifleştirir (soft-delete). */
    private void deactivateLinks(Long campaignId) {
        List<CampaignOffer> links = campaignOfferRepository.findAllByCampaignIdAndDeletedDateIsNull(campaignId);
        GeneralStatus deletedStatus = referenceDataService.getStatus(
                ProductReferenceCodes.ENTITY_CAMPAIGN_OFFER, ProductReferenceCodes.STATUS_DELETED_CODE);
        LocalDateTime now = LocalDateTime.now();
        for (CampaignOffer link : links) {
            link.setGeneralStatus(deletedStatus);
            link.setDeletedDate(now);
        }
        campaignOfferRepository.saveAll(links);
    }

    /** Kampanyayı, paket içeriği ve fiyat toplamlarıyla birlikte yanıta dönüştürür. */
    private CampaignResponse buildResponse(Campaign campaign) {
        List<Long> offerIds = campaignOfferRepository.findAllByCampaignIdAndDeletedDateIsNull(campaign.getId())
                .stream()
                .map(link -> link.getProductOffer().getId())
                .toList();

        List<CampaignOfferLine> offers = offerIds.isEmpty()
                ? List.of()
                : productOfferRepository.findAllByIdInAndDeletedDateIsNull(offerIds).stream()
                        .sorted(Comparator.comparing(ProductOffer::getId))
                        .map(o -> new CampaignOfferLine(o.getId(), o.getName(), o.getPrice()))
                        .toList();

        BigDecimal listPriceTotal = offers.stream()
                .map(CampaignOfferLine::listPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal savings = listPriceTotal.subtract(campaign.getCampaignPrice());

        return new CampaignResponse(
                campaign.getId(),
                campaign.getName(),
                campaign.getCampaignPrice(),
                listPriceTotal,
                savings,
                offers,
                campaign.getGeneralStatus().getShortCode(),
                campaign.getCreatedDate(),
                campaign.getUpdatedDate());
    }

    @Override
    @Transactional(readOnly = true)
    public Campaign getCampaignById(Long id) {
        return findActiveOrThrow(id);
    }

    private Campaign findActiveOrThrow(Long id) {
        return repository.findByIdAndDeletedDateIsNull(id)
                .orElseThrow(() -> new BusinessException(Messages.CAMPAIGN_NOT_FOUND));
    }
}
