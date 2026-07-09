package com.etiya.productservice.business.concretes;

import com.etiya.productservice.business.abstracts.CampaignService;
import com.etiya.productservice.business.constants.Messages;
import com.etiya.productservice.business.dtos.requests.CreateCampaignRequest;
import com.etiya.productservice.business.dtos.requests.UpdateCampaignRequest;
import com.etiya.productservice.business.dtos.responses.CampaignResponse;
import com.etiya.productservice.business.dtos.responses.PagedResponse;
import com.etiya.productservice.business.mappers.CampaignMapper;
import com.etiya.productservice.core.constants.CacheNames;
import com.etiya.productservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.productservice.dataAccess.CampaignRepository;
import com.etiya.productservice.entities.Campaign;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Kampanya iş mantığı (business/concretes). Soft-delete ve Redis cache uygulanır.
 */
@Service
public class CampaignManager implements CampaignService {

    private final CampaignRepository repository;
    private final CampaignMapper mapper;

    public CampaignManager(CampaignRepository repository, CampaignMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.CAMPAIGN_LIST, allEntries = true)
    public CampaignResponse add(CreateCampaignRequest request) {
        Campaign entity = mapper.toEntity(request);
        entity.setIsActive(true);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CAMPAIGNS, key = "#id")
    public CampaignResponse getById(Long id) {
        return mapper.toResponse(findActiveOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CAMPAIGN_LIST,
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public PagedResponse<CampaignResponse> getAll(Pageable pageable) {
        return PagedResponse.of(repository.findAllByIsActiveTrue(pageable).map(mapper::toResponse));
    }

    @Override
    @Transactional
    @Caching(
            put = @CachePut(value = CacheNames.CAMPAIGNS, key = "#id"),
            evict = @CacheEvict(value = CacheNames.CAMPAIGN_LIST, allEntries = true)
    )
    public CampaignResponse update(Long id, UpdateCampaignRequest request) {
        Campaign entity = findActiveOrThrow(id);
        entity.setName(request.name());
        entity.setTotalPrice(request.totalPrice());
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.CAMPAIGNS, key = "#id"),
            @CacheEvict(value = CacheNames.CAMPAIGN_LIST, allEntries = true)
    })
    public void delete(Long id) {
        Campaign entity = findActiveOrThrow(id);
        entity.setIsActive(false);
        entity.setDeletedDate(LocalDateTime.now());
        repository.save(entity);
    }

    private Campaign findActiveOrThrow(Long id) {
        return repository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException(Messages.CAMPAIGN_NOT_FOUND));
    }
}
