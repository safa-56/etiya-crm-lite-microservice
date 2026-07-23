package com.etiya.customerservice.business.concretes;

import com.etiya.customerservice.business.abstracts.AddressService;
import com.etiya.customerservice.business.abstracts.IndividualCustomerService;
import com.etiya.customerservice.business.abstracts.OutboxService;
import com.etiya.customerservice.business.abstracts.ReferenceDataService;
import com.etiya.customerservice.business.constants.CustomerEvents;
import com.etiya.customerservice.business.constants.PartyReferenceCodes;
import com.etiya.customerservice.business.dtos.events.CustomerEventPayload;
import com.etiya.customerservice.business.dtos.requests.CreateAddressRequest;
import com.etiya.customerservice.business.dtos.requests.UpdateAddressRequest;
import com.etiya.customerservice.business.dtos.responses.AddressResponse;
import com.etiya.customerservice.business.mappers.AddressMapper;
import com.etiya.customerservice.business.rules.AddressBusinessRules;
import com.etiya.customerservice.dataAccess.AddressRepository;
import com.etiya.customerservice.entities.Address;
import com.etiya.customerservice.entities.Customer;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Adres iş servisi uygulaması (business concrete).
 *
 * <p>CRUD işlemlerini yürütür; iş kurallarını {@link AddressBusinessRules}'a
 * delege eder, DTO/entity dönüşümünü {@link AddressMapper} ile yapar ve
 * silme işleminde soft-delete uygular.
 *
 * <p>Not: Adres, {@code Customer} agregasının bir çocuğudur; müşteri cache'i
 * ({@code individualCustomers}) adresleri gömülü tuttuğundan, tutarsızlık
 * (stale) riskini önlemek için burada bağımsız bir cache kullanılmaz.
 */
@Service
@RequiredArgsConstructor
public class AddressManager implements AddressService {

    private final AddressRepository repository;
    private final IndividualCustomerService individualCustomerService;
    private final AddressMapper mapper;
    private final AddressBusinessRules rules;
    private final ReferenceDataService referenceDataService;

    @Override
    @Transactional
    public AddressResponse add(CreateAddressRequest request) {
        rules.checkIfCustomerExists(request.customerId());

        Customer customer = individualCustomerService.getIndividualCustomerById(request.customerId());

        Address address = mapper.toEntity(request);
        address.setCustomer(customer);
        address.setGeneralStatus(referenceDataService.getStatus(
                PartyReferenceCodes.ENTITY_ADDRESS, PartyReferenceCodes.STATUS_ACTIVE_CODE));

        // Bu adres birincil olarak ekleniyorsa, müşterinin diğer birincil
        // adreslerini düşür (en fazla bir birincil adres — FR-006 ACC-07).
        if (Boolean.TRUE.equals(address.getIsPrimary())) {
            rules.demoteExistingPrimaryAddresses(customer.getId(), null);
        }

        Address saved = repository.save(address);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getById(Long id) {
        Address address = rules.checkAddressIfExists(id);
        return mapper.toResponse(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAll() {
        return repository.findAllByDeletedDateIsNull().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponse update(Long id, UpdateAddressRequest request) {
        Address address = rules.checkAddressIfExists(id);

        // address.setCity(request.city());
        // address.setStreet(request.street());
        // address.setHouseNumber(request.houseNumber());
        // address.setAddressDescription(request.addressDescription());

        mapper.updateEntity(address, request);

        if (Boolean.TRUE.equals(request.isPrimary())) {
            // Bu adres birincil yapılıyorsa, aynı müşterinin daha önce birincil
            // işaretlenmiş adresinin bayrağını kaldır (FR-006 ACC-07).
            rules.demoteExistingPrimaryAddresses(address.getCustomer().getId(), address.getId());
            address.setIsPrimary(true);

        } else if (request.isPrimary() != null) {
            address.setIsPrimary(request.isPrimary());
        }

        Address saved = repository.save(address);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Address address = rules.checkAddressIfExists(id);

        // Soft-delete: fiziksel silme yok; durumu DEL yap ve silinme zamanını işaretle.
        address.setGeneralStatus(referenceDataService.getStatus(
                PartyReferenceCodes.ENTITY_ADDRESS, PartyReferenceCodes.STATUS_DELETED_CODE));
        address.setDeletedDate(LocalDateTime.now());
        repository.save(address);

    }
}
