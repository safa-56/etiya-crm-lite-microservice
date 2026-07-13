package com.etiya.productservice.business.abstracts;

import com.etiya.productservice.business.dtos.requests.CreateCampaignRequest;
import com.etiya.productservice.business.dtos.requests.UpdateCampaignRequest;
import com.etiya.productservice.business.dtos.responses.CampaignResponse;
import com.etiya.productservice.business.dtos.responses.PagedResponse;
import com.etiya.productservice.entities.Campaign;
import org.springframework.data.domain.Pageable;

/**
 * Kampanya iş servisi (business abstraction). CRUD işlemlerini tanımlar.
 */
public interface CampaignService {

    CampaignResponse add(CreateCampaignRequest request);

    CampaignResponse getById(Long id);

    /** İlişki kurulumu için aktif kampanya entity'sini döner; yoksa iş hatası fırlatır. */
    Campaign getCampaignById(Long id);

    PagedResponse<CampaignResponse> getAll(Pageable pageable);

    CampaignResponse update(Long id, UpdateCampaignRequest request);

    void delete(Long id);
}
