package com.etiya.customerservice.business.abstracts;

import com.etiya.customerservice.business.dtos.requests.CreateContactInfoRequest;
import com.etiya.customerservice.business.dtos.requests.UpdateContactInfoRequest;
import com.etiya.customerservice.business.dtos.responses.ContactInfoResponse;

import java.util.List;

/**
 * Müşteri iletişim bilgisi iş servisi (business abstraction). CRUD işlemlerini tanımlar.
 */
public interface CustomerContactInfoService {

    ContactInfoResponse add(CreateContactInfoRequest request);

    ContactInfoResponse getById(Long id);

    List<ContactInfoResponse> getAll();

    ContactInfoResponse update(UpdateContactInfoRequest request);

    void delete(Long id);
}
