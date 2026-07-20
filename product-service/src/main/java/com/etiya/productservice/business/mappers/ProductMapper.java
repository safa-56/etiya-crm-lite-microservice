package com.etiya.productservice.business.mappers;

import com.etiya.productservice.business.dtos.responses.ProductResponse;
import com.etiya.productservice.entities.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Product için entity -> yanıt eşlemesi (MapStruct).
 *
 * <p>Oluşturma sırasında ilişkiler (productOffer, campaign) manager'da
 * çözüldüğünden {@code toEntity} sağlanmaz; yalnızca yanıt düzleştirmesi yapılır.
 * {@code campaign} boş olabileceğinden MapStruct null-güvenli olarak {@code campaignId}'yi
 * boş bırakır.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "productOfferId", source = "productOffer.id")
    @Mapping(target = "campaignId", source = "campaign.id")
    @Mapping(target = "status", source = "generalStatus.shortCode")
    ProductResponse toResponse(Product entity);
}
