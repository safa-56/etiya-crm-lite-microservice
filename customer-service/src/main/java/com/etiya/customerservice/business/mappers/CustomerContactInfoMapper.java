package com.etiya.customerservice.business.mappers;

import com.etiya.customerservice.business.dtos.requests.CreateContactInfoRequest;
import com.etiya.customerservice.business.dtos.responses.ContactInfoResponse;
import com.etiya.customerservice.entities.CustomerContactInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * CustomerContactInfo için DTO <-> entity eşlemeleri (MapStruct).
 *
 * <p>İlişkili {@code customer} ve audit/id alanları mapper'da yok sayılır;
 * müşteri ilişkisi business (manager) katmanında {@code customerId} üzerinden
 * kurulur.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerContactInfoMapper {

    @Mapping(target = "customer", ignore = true)
    CustomerContactInfo toEntity(CreateContactInfoRequest request);

    ContactInfoResponse toResponse(CustomerContactInfo entity);
}
