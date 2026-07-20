package com.etiya.customerservice.business.mappers;

import com.etiya.customerservice.business.dtos.requests.CreateAddressRequest;
import com.etiya.customerservice.business.dtos.requests.CreateContactInfoRequest;
import com.etiya.customerservice.business.dtos.requests.CreateIndividualCustomerRequest;
import com.etiya.customerservice.business.dtos.responses.AddressResponse;
import com.etiya.customerservice.business.dtos.responses.ContactInfoResponse;
import com.etiya.customerservice.business.dtos.responses.IndividualCustomerResponse;
import com.etiya.customerservice.entities.Address;
import com.etiya.customerservice.entities.CustomerContactInfo;
import com.etiya.customerservice.entities.IndividualCustomer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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

    // --- request -> entity (yalnızca skaler alanlar; ilişkiler manager'da) ---

    @Mapping(target = "contactInfos", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    IndividualCustomer toEntity(CreateIndividualCustomerRequest request);

    @Mapping(target = "customer", ignore = true)
    CustomerContactInfo toContactInfo(CreateContactInfoRequest request);

    @Mapping(target = "customer", ignore = true)
    Address toAddress(CreateAddressRequest request);

    // --- entity -> response ---

    @Mapping(target = "status", source = "generalStatus.shortCode")
    IndividualCustomerResponse toResponse(IndividualCustomer entity);

    @Mapping(target = "customerId", source = "customer.id")
    ContactInfoResponse toContactInfoResponse(CustomerContactInfo entity);

    @Mapping(target = "customerId", source = "customer.id")
    AddressResponse toAddressResponse(Address entity);
}
