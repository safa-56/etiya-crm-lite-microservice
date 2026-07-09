package com.etiya.cartservice.business.mappers;

import com.etiya.cartservice.business.dtos.requests.CreateCartRequest;
import com.etiya.cartservice.entities.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Cart için DTO -> entity eşlemesi (MapStruct).
 *
 * <p>Kimlik/audit alanları ve satır koleksiyonu ({@code items}) mapper'da yok
 * sayılır; bunlar business (manager) katmanında yönetilir. Yanıt (response)
 * tarafındaki türetilmiş alanlar (toplam tutar, kampanya içeriği) projeksiyon
 * gerektirdiğinden manager içindeki {@code buildResponse} ile elle kurulur.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

    @Mapping(target = "items", ignore = true)
    Cart toEntity(CreateCartRequest request);
}
