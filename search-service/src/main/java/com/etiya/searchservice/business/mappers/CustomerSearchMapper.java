package com.etiya.searchservice.business.mappers;

import com.etiya.searchservice.business.dtos.responses.CustomerSearchResponse;
import com.etiya.searchservice.entities.CustomerSearchIndex;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Arama indeksi entity'sinden sonuç DTO'suna eşleme (MapStruct).
 *
 * <p>Sonuç yalnızca FR-002 ACC-21 kolonlarını taşır; koleksiyon alanları
 * ({@code accountNumbers}/{@code orderNumbers}) sonuç satırında gösterilmez.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerSearchMapper {

    CustomerSearchResponse toResponse(CustomerSearchIndex entity);
}
