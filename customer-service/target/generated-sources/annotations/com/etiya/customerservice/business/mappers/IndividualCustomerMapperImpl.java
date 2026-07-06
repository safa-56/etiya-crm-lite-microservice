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
import com.etiya.customerservice.entities.enums.GenderType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-06T16:05:23+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.3 (Oracle Corporation)"
)
@Component
public class IndividualCustomerMapperImpl implements IndividualCustomerMapper {

    @Override
    public IndividualCustomer toEntity(CreateIndividualCustomerRequest request) {
        if ( request == null ) {
            return null;
        }

        IndividualCustomer individualCustomer = new IndividualCustomer();

        individualCustomer.setGenderId( request.genderId() );
        individualCustomer.setNationalityId( request.nationalityId() );
        individualCustomer.setFirstName( request.firstName() );
        individualCustomer.setSecondName( request.secondName() );
        individualCustomer.setLastName( request.lastName() );
        individualCustomer.setBirthDate( request.birthDate() );
        individualCustomer.setFatherName( request.fatherName() );
        individualCustomer.setMotherName( request.motherName() );
        individualCustomer.setGenderType( request.genderType() );

        return individualCustomer;
    }

    @Override
    public CustomerContactInfo toContactInfo(CreateContactInfoRequest request) {
        if ( request == null ) {
            return null;
        }

        CustomerContactInfo customerContactInfo = new CustomerContactInfo();

        customerContactInfo.setEmail( request.email() );
        customerContactInfo.setHomePhone( request.homePhone() );
        customerContactInfo.setMobilPhone( request.mobilPhone() );
        customerContactInfo.setFax( request.fax() );

        return customerContactInfo;
    }

    @Override
    public Address toAddress(CreateAddressRequest request) {
        if ( request == null ) {
            return null;
        }

        Address address = new Address();

        address.setCity( request.city() );
        address.setStreet( request.street() );
        address.setHouseNumber( request.houseNumber() );
        address.setAddressDescription( request.addressDescription() );
        address.setIsPrimary( request.isPrimary() );

        return address;
    }

    @Override
    public IndividualCustomerResponse toResponse(IndividualCustomer entity) {
        if ( entity == null ) {
            return null;
        }

        Long id = null;
        String firstName = null;
        String secondName = null;
        String lastName = null;
        LocalDate birthDate = null;
        String fatherName = null;
        String motherName = null;
        Long genderId = null;
        Long nationalityId = null;
        GenderType genderType = null;
        Boolean isActive = null;
        LocalDateTime createdDate = null;
        LocalDateTime updatedDate = null;
        List<ContactInfoResponse> contactInfos = null;
        List<AddressResponse> addresses = null;

        id = entity.getId();
        firstName = entity.getFirstName();
        secondName = entity.getSecondName();
        lastName = entity.getLastName();
        birthDate = entity.getBirthDate();
        fatherName = entity.getFatherName();
        motherName = entity.getMotherName();
        genderId = entity.getGenderId();
        nationalityId = entity.getNationalityId();
        genderType = entity.getGenderType();
        isActive = entity.getIsActive();
        createdDate = entity.getCreatedDate();
        updatedDate = entity.getUpdatedDate();
        contactInfos = customerContactInfoListToContactInfoResponseList( entity.getContactInfos() );
        addresses = addressListToAddressResponseList( entity.getAddresses() );

        IndividualCustomerResponse individualCustomerResponse = new IndividualCustomerResponse( id, firstName, secondName, lastName, birthDate, fatherName, motherName, genderId, nationalityId, genderType, isActive, createdDate, updatedDate, contactInfos, addresses );

        return individualCustomerResponse;
    }

    @Override
    public ContactInfoResponse toContactInfoResponse(CustomerContactInfo entity) {
        if ( entity == null ) {
            return null;
        }

        Long id = null;
        String email = null;
        String homePhone = null;
        String mobilPhone = null;
        String fax = null;

        id = entity.getId();
        email = entity.getEmail();
        homePhone = entity.getHomePhone();
        mobilPhone = entity.getMobilPhone();
        fax = entity.getFax();

        ContactInfoResponse contactInfoResponse = new ContactInfoResponse( id, email, homePhone, mobilPhone, fax );

        return contactInfoResponse;
    }

    @Override
    public AddressResponse toAddressResponse(Address entity) {
        if ( entity == null ) {
            return null;
        }

        Long id = null;
        String city = null;
        String street = null;
        String houseNumber = null;
        String addressDescription = null;
        Boolean isPrimary = null;

        id = entity.getId();
        city = entity.getCity();
        street = entity.getStreet();
        houseNumber = entity.getHouseNumber();
        addressDescription = entity.getAddressDescription();
        isPrimary = entity.getIsPrimary();

        AddressResponse addressResponse = new AddressResponse( id, city, street, houseNumber, addressDescription, isPrimary );

        return addressResponse;
    }

    protected List<ContactInfoResponse> customerContactInfoListToContactInfoResponseList(List<CustomerContactInfo> list) {
        if ( list == null ) {
            return null;
        }

        List<ContactInfoResponse> list1 = new ArrayList<ContactInfoResponse>( list.size() );
        for ( CustomerContactInfo customerContactInfo : list ) {
            list1.add( toContactInfoResponse( customerContactInfo ) );
        }

        return list1;
    }

    protected List<AddressResponse> addressListToAddressResponseList(List<Address> list) {
        if ( list == null ) {
            return null;
        }

        List<AddressResponse> list1 = new ArrayList<AddressResponse>( list.size() );
        for ( Address address : list ) {
            list1.add( toAddressResponse( address ) );
        }

        return list1;
    }
}
