package com.etiya.customerservice.business.abstracts;

import com.etiya.customerservice.business.dtos.requests.CreateIndividualCustomerRequest;
import com.etiya.customerservice.business.dtos.requests.UpdateIndividualCustomerRequest;
import com.etiya.customerservice.business.dtos.responses.IndividualCustomerResponse;

import java.util.List;

/**
 * Bireysel müşteri iş servisi (business abstraction). CRUD işlemlerini tanımlar.
 */
public interface IndividualCustomerService {

    IndividualCustomerResponse add(CreateIndividualCustomerRequest request);

    IndividualCustomerResponse getById(Long id);

    List<IndividualCustomerResponse> getAll();

    IndividualCustomerResponse update(UpdateIndividualCustomerRequest request);

    void delete(Long id);
}
