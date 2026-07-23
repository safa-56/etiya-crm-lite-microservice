package com.etiya.customerservice.business.mappers;

import com.etiya.customerservice.business.dtos.requests.CreateAddressRequest;
import com.etiya.customerservice.business.dtos.requests.CreateContactInfoRequest;
import com.etiya.customerservice.business.dtos.requests.CreateIndividualCustomerRequest;
import com.etiya.customerservice.business.dtos.requests.UpdateIndividualCustomerRequest;
import com.etiya.customerservice.business.dtos.responses.AddressResponse;
import com.etiya.customerservice.business.dtos.responses.ContactInfoResponse;
import com.etiya.customerservice.business.dtos.responses.IndividualCustomerResponse;
import com.etiya.customerservice.entities.Address;
import com.etiya.customerservice.entities.CustomerContactInfo;
import com.etiya.customerservice.entities.IndividualCustomer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * IndividualCustomer için DTO <-> entity eşlemeleri (MapStruct).
 *
 * <p>İç içe koleksiyonların ({@code contactInfos}, {@code addresses}) geri
 * referansı ({@code customer}) ve audit/id alanları mapper'da <b>yok sayılır</b>;
 * ilişki kurulumu ve kimlik ataması business (manager) katmanında yapılır.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IndividualCustomerMapper {

    @Mapping(target = "contactInfos", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    IndividualCustomer toEntity(CreateIndividualCustomerRequest request);

    @Mapping(target = "status", source = "generalStatus.shortCode")
    IndividualCustomerResponse toResponse(IndividualCustomer entity);

    void updateEntity(@MappingTarget IndividualCustomer individualCustomer, UpdateIndividualCustomerRequest updateIndividualCustomerRequest);
}
