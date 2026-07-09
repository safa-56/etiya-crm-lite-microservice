package com.etiya.customerservice.business.mappers;

import com.etiya.customerservice.business.dtos.requests.CreateAddressRequest;
import com.etiya.customerservice.business.dtos.responses.AddressResponse;
import com.etiya.customerservice.entities.Address;
import com.etiya.customerservice.entities.Customer;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-09T10:12:17+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.3 (Oracle Corporation)"
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

        Long customerId = null;
        Long id = null;
        String city = null;
        String street = null;
        String houseNumber = null;
        String addressDescription = null;
        Boolean isPrimary = null;

        customerId = entityCustomerId( entity );
        id = entity.getId();
        city = entity.getCity();
        street = entity.getStreet();
        houseNumber = entity.getHouseNumber();
        addressDescription = entity.getAddressDescription();
        isPrimary = entity.getIsPrimary();

        AddressResponse addressResponse = new AddressResponse( id, customerId, city, street, houseNumber, addressDescription, isPrimary );

        return addressResponse;
    }

    private Long entityCustomerId(Address address) {
        Customer customer = address.getCustomer();
        if ( customer == null ) {
            return null;
        }
        return customer.getId();
    }
}
