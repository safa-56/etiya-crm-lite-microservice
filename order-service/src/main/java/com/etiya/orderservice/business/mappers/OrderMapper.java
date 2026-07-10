package com.etiya.orderservice.business.mappers;

import com.etiya.orderservice.business.dtos.requests.SubmitOrderRequest;
import com.etiya.orderservice.entities.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Order için DTO -> entity eşlemesi (MapStruct).
 *
 * <p>Yalnızca istekte gelen alanlar ({@code cartId}, {@code serviceAddressId},
 * {@code serviceAddress}) eşlenir. Kimlik/audit alanları, sistem tarafından üretilen
 * {@code orderNumber}, saga ile dolan {@code status}/{@code totalAmount}/{@code customerId}/
 * {@code accountId} ve satır koleksiyonu ({@code items}) mapper'da yok sayılır; bunlar
 * business (manager) katmanında set edilir. Yanıt tarafındaki türetilmiş alanlar
 * (satır snapshot'ı, toplam) projeksiyon gerektirdiğinden manager içindeki
 * {@code buildResponse} ile elle kurulur.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "items", ignore = true)
    Order toEntity(SubmitOrderRequest request);
}
