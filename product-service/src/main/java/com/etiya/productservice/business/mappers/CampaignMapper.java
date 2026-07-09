package com.etiya.productservice.business.mappers;

import com.etiya.productservice.business.dtos.requests.CreateCampaignRequest;
import com.etiya.productservice.entities.Campaign;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Campaign için DTO -> entity eşlemesi (MapStruct).
 *
 * <p>Yanıt ({@link com.etiya.productservice.business.dtos.responses.CampaignResponse})
 * paket içeriği ve fiyat toplamları gibi türetilmiş alanlar içerdiğinden manager
 * katmanında elle kurulur; burada yalnızca skaler alanlar (name, campaignPrice)
 * entity'ye map edilir ({@code offerIds} yok sayılır).
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CampaignMapper {

    Campaign toEntity(CreateCampaignRequest request);
}
