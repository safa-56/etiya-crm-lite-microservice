package com.etiya.productservice.business.mappers;

import com.etiya.productservice.business.dtos.requests.CreateCatalogRequest;
import com.etiya.productservice.business.dtos.responses.CatalogResponse;
import com.etiya.productservice.entities.Catalog;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Catalog için DTO <-> entity eşlemeleri (MapStruct).
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CatalogMapper {

    Catalog toEntity(CreateCatalogRequest request);

    CatalogResponse toResponse(Catalog entity);
}
