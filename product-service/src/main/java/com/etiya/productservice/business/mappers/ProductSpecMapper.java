package com.etiya.productservice.business.mappers;

import com.etiya.productservice.business.dtos.requests.CreateProductSpecRequest;
import com.etiya.productservice.business.dtos.responses.ProductSpecResponse;
import com.etiya.productservice.entities.ProductSpec;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * ProductSpec için DTO <-> entity eşlemeleri (MapStruct).
 *
 * <p>Kimlik/audit alanları (id, tarihler, isActive) mapper'da yok sayılır;
 * bunlar business (manager) ve {@code BaseEntity} yaşam döngüsü tarafından set edilir.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductSpecMapper {

    ProductSpec toEntity(CreateProductSpecRequest request);

    ProductSpecResponse toResponse(ProductSpec entity);
}
