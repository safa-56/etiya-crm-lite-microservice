package com.etiya.customerservice.business.mappers;

import com.etiya.customerservice.business.dtos.requests.CreateAddressRequest;
import com.etiya.customerservice.business.dtos.responses.AddressResponse;
import com.etiya.customerservice.entities.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Address için DTO <-> entity eşlemeleri (MapStruct).
 *
 * <p>İlişkili {@code customer} ve audit/id alanları mapper'da yok sayılır;
 * müşteri ilişkisi business (manager) katmanında {@code customerId} üzerinden
 * kurulur.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AddressMapper {

    @Mapping(target = "customer", ignore = true)
    Address toEntity(CreateAddressRequest request);

    @Mapping(target = "customerId", source = "customer.id")
    AddressResponse toResponse(Address entity);
}
