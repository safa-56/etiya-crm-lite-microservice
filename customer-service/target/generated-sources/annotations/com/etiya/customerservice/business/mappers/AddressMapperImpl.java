package com.etiya.customerservice.business.mappers;

import com.etiya.customerservice.business.dtos.requests.CreateAddressRequest;
import com.etiya.customerservice.business.dtos.responses.AddressResponse;
import com.etiya.customerservice.entities.Address;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-07T08:41:04+0300",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class AddressMapperImpl implements AddressMapper {

    @Override
    public Address toEntity(CreateAddressRequest request) {
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
    public AddressResponse toResponse(Address entity) {
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
}
