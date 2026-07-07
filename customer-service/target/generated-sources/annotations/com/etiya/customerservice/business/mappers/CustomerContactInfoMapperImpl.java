package com.etiya.customerservice.business.mappers;

import com.etiya.customerservice.business.dtos.requests.CreateContactInfoRequest;
import com.etiya.customerservice.business.dtos.responses.ContactInfoResponse;
import com.etiya.customerservice.entities.CustomerContactInfo;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-07T10:08:35+0300",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
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
        customerContactInfo.setFax( request.fax() );
        customerContactInfo.setHomePhone( request.homePhone() );
        customerContactInfo.setMobilPhone( request.mobilPhone() );

        return customerContactInfo;
    }

    @Override
    public ContactInfoResponse toResponse(CustomerContactInfo entity) {
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
}
