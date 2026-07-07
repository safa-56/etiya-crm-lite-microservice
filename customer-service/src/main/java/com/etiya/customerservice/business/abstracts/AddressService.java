package com.etiya.customerservice.business.abstracts;

import com.etiya.customerservice.business.dtos.requests.CreateAddressRequest;
import com.etiya.customerservice.business.dtos.requests.UpdateAddressRequest;
import com.etiya.customerservice.business.dtos.responses.AddressResponse;

import java.util.List;

/**
 * Adres iş servisi (business abstraction). CRUD işlemlerini tanımlar.
 */
public interface AddressService {

    AddressResponse add(CreateAddressRequest request);

    AddressResponse getById(Long id);

    List<AddressResponse> getAll();

    AddressResponse update(UpdateAddressRequest request);

    void delete(Long id);
}
