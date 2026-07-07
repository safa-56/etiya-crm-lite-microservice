package com.etiya.customerservice.business.concretes;

import com.etiya.customerservice.business.abstracts.AddressService;
import com.etiya.customerservice.business.constants.Messages;
import com.etiya.customerservice.business.dtos.requests.CreateAddressRequest;
import com.etiya.customerservice.business.dtos.requests.UpdateAddressRequest;
import com.etiya.customerservice.business.dtos.responses.AddressResponse;
import com.etiya.customerservice.business.mappers.AddressMapper;
import com.etiya.customerservice.business.rules.AddressBusinessRules;
import com.etiya.customerservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.customerservice.dataAccess.AddressRepository;
import com.etiya.customerservice.dataAccess.CustomerRepository;
import com.etiya.customerservice.entities.Address;
import com.etiya.customerservice.entities.Customer;
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
public class AddressManager implements AddressService {

    private final AddressRepository repository;
    private final CustomerRepository customerRepository;
    private final AddressMapper mapper;
    private final AddressBusinessRules rules;

    public AddressManager(AddressRepository repository,
                          CustomerRepository customerRepository,
                          AddressMapper mapper,
                          AddressBusinessRules rules) {
        this.repository = repository;
        this.customerRepository = customerRepository;
        this.mapper = mapper;
        this.rules = rules;
    }

    @Override
    @Transactional
    public AddressResponse add(CreateAddressRequest request) {
        // --- iş kuralları ---
        rules.checkIfCustomerExists(request.customerId());

        // --- dönüşüm + ilişki kurulumu ---
        Customer customer = customerRepository.findByIdAndIsActiveTrue(request.customerId())
                .orElseThrow(() -> new BusinessException(Messages.CUSTOMER_NOT_FOUND));

        Address address = mapper.toEntity(request);
        address.setCustomer(customer);

        Address saved = repository.save(address);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getById(Long id) {
        return mapper.toResponse(findActiveAddress(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAll() {
        return repository.findAllByIsActiveTrue().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponse update(UpdateAddressRequest request) {
        rules.checkIfAddressExists(request.id());

        Address address = findActiveAddress(request.id());

        // Skaler alanları güncelle (müşteri ilişkisi değiştirilmez).
        address.setCity(request.city());
        address.setStreet(request.street());
        address.setHouseNumber(request.houseNumber());
        address.setAddressDescription(request.addressDescription());
        if (request.isPrimary() != null) {
            address.setIsPrimary(request.isPrimary());
        }

        return mapper.toResponse(repository.save(address));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Address address = findActiveAddress(id);

        // Soft-delete: fiziksel silme yok; pasifleştir ve silinme zamanını işaretle.
        address.setIsActive(false);
        address.setDeletedDate(LocalDateTime.now());
        repository.save(address);
    }

    // ------------------------------------------------------------------ yardımcılar

    private Address findActiveAddress(Long id) {
        return repository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException(Messages.ADDRESS_NOT_FOUND));
    }
}
