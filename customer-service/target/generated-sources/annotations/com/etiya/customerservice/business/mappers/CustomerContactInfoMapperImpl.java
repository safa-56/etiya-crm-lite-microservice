package com.etiya.customerservice.business.mappers;

import com.etiya.customerservice.business.dtos.requests.CreateContactInfoRequest;
import com.etiya.customerservice.business.dtos.responses.ContactInfoResponse;
import com.etiya.customerservice.entities.Customer;
import com.etiya.customerservice.entities.CustomerContactInfo;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-09T10:12:17+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.3 (Oracle Corporation)"
)
@Component
public class CustomerContactInfoMapperImpl implements CustomerContactInfoMapper {

    @Override
    public CustomerContactInfo toEntity(CreateContactInfoRequest request) {
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
    public ContactInfoResponse toResponse(CustomerContactInfo entity) {
        if ( entity == null ) {
            return null;
        }

        Long customerId = null;
        Long id = null;
        String email = null;
        String homePhone = null;
        String mobilPhone = null;
        String fax = null;

        customerId = entityCustomerId( entity );
        id = entity.getId();
        email = entity.getEmail();
        homePhone = entity.getHomePhone();
        mobilPhone = entity.getMobilPhone();
        fax = entity.getFax();

        ContactInfoResponse contactInfoResponse = new ContactInfoResponse( id, customerId, email, homePhone, mobilPhone, fax );

        return contactInfoResponse;
    }

    private Long entityCustomerId(CustomerContactInfo customerContactInfo) {
        Customer customer = customerContactInfo.getCustomer();
        if ( customer == null ) {
            return null;
        }
        return customer.getId();
    }
}
