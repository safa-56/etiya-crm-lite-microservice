package com.etiya.productservice.business.mappers;

import com.etiya.productservice.business.dtos.requests.CreateProductOfferRequest;
import com.etiya.productservice.business.dtos.responses.ProductOfferResponse;
import com.etiya.productservice.entities.ProductOffer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * ProductOffer için DTO <-> entity eşlemeleri (MapStruct).
 *
 * <p>{@code productSpec} ilişkisi mapper'da yok sayılır; ilgili {@link com.etiya.productservice.entities.ProductSpec}
 * entity'si business (manager) katmanında çözülüp set edilir. Yanıtta ilişkiden
 * yalnızca id ve ad düzleştirilerek taşınır.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductOfferMapper {

    // İlişkiler (catalog, productSpec) manager'da set edilir; skaler alanlar map edilir.
    ProductOffer toEntity(CreateProductOfferRequest request);

    @Mapping(target = "catalogId", source = "catalog.id")
    @Mapping(target = "catalogName", source = "catalog.name")
    @Mapping(target = "productSpecId", source = "productSpec.id")
    @Mapping(target = "productSpecName", source = "productSpec.name")
    ProductOfferResponse toResponse(ProductOffer entity);
}
