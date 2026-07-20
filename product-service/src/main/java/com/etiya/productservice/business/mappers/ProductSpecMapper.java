package com.etiya.productservice.business.mappers;

import com.etiya.productservice.business.dtos.requests.CreateProductSpecRequest;
import com.etiya.productservice.business.dtos.responses.ProductSpecResponse;
import com.etiya.productservice.entities.ProductSpec;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * ProductSpec için DTO <-> entity eşlemeleri (MapStruct).
 *
 * <p>Kimlik/audit alanları (id, tarihler) mapper'da yok sayılır; bunlar business
 * (manager) ve {@code BaseEntity} yaşam döngüsü tarafından set edilir. Durum FK'si
 * ({@code generalStatus}) manager'da {@code ReferenceDataService} ile çözülüp set edilir.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductSpecMapper {

    ProductSpec toEntity(CreateProductSpecRequest request);

    @Mapping(target = "status", source = "generalStatus.shortCode")
    ProductSpecResponse toResponse(ProductSpec entity);
}
