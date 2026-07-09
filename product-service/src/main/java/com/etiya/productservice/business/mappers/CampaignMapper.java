package com.etiya.productservice.business.mappers;

import com.etiya.productservice.business.dtos.requests.CreateCampaignRequest;
import com.etiya.productservice.business.dtos.responses.CampaignResponse;
import com.etiya.productservice.entities.Campaign;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Campaign için DTO <-> entity eşlemeleri (MapStruct).
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CampaignMapper {

    Campaign toEntity(CreateCampaignRequest request);

    CampaignResponse toResponse(Campaign entity);
}
