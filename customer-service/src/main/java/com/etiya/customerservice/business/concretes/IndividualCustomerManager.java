package com.etiya.customerservice.business.concretes;

import com.etiya.customerservice.business.abstracts.IndividualCustomerService;
import com.etiya.customerservice.business.abstracts.OutboxService;
import com.etiya.customerservice.business.constants.CustomerEvents;
import com.etiya.customerservice.business.constants.Messages;
import com.etiya.customerservice.business.dtos.events.CustomerEventPayload;
import com.etiya.customerservice.business.dtos.requests.CreateAddressRequest;
import com.etiya.customerservice.business.dtos.requests.CreateContactInfoRequest;
import com.etiya.customerservice.business.dtos.requests.CreateIndividualCustomerRequest;
import com.etiya.customerservice.business.dtos.requests.UpdateIndividualCustomerRequest;
import com.etiya.customerservice.business.dtos.responses.IndividualCustomerResponse;
import com.etiya.customerservice.business.mappers.IndividualCustomerMapper;
import com.etiya.customerservice.business.rules.IndividualCustomerBusinessRules;
import com.etiya.customerservice.core.constants.CacheNames;
import com.etiya.customerservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.customerservice.dataAccess.IndividualCustomerRepository;
import com.etiya.customerservice.entities.IndividualCustomer;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Bireysel müşteri iş servisi uygulaması (business concrete).
 *
 * <p>CRUD işlemlerini yürütür; iş kurallarını {@link IndividualCustomerBusinessRules}'a
 * delege eder, DTO/entity dönüşümünü {@link IndividualCustomerMapper} ile yapar,
 * cacheleme için Redis (Spring Cache) kullanır ve her yazma işleminde
 * {@link OutboxService} üzerinden (aynı transaction'da) bir domain olayı yazar
 * (Transactional Outbox → Debezium → Kafka Cloud).
 */
@Service
public class IndividualCustomerManager implements IndividualCustomerService {

    private final IndividualCustomerRepository repository;
    private final IndividualCustomerMapper mapper;
    private final IndividualCustomerBusinessRules rules;
    private final OutboxService outboxService;

    public IndividualCustomerManager(IndividualCustomerRepository repository,
                                     IndividualCustomerMapper mapper,
                                     IndividualCustomerBusinessRules rules,
                                     OutboxService outboxService) {
        this.repository = repository;
        this.mapper = mapper;
        this.rules = rules;
        this.outboxService = outboxService;
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.INDIVIDUAL_CUSTOMER_LIST, allEntries = true)
    public IndividualCustomerResponse add(CreateIndividualCustomerRequest request) {
        // --- iş kuralları ---
        rules.checkIfBirthDateValid(request.birthDate());
        rules.checkIfEmailsAlreadyExist(extractEmails(request.contactInfos()));

        // --- dönüşüm + ilişki kurulumu ---
        IndividualCustomer customer = mapper.toEntity(request);
        applyContactInfos(customer, request.contactInfos());
        applyAddresses(customer, request.addresses());

        IndividualCustomer saved = repository.save(customer);

        // --- outbox olayı (aynı transaction) ---
        publishEvent(saved, CustomerEvents.CUSTOMER_CREATED);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.INDIVIDUAL_CUSTOMERS, key = "#id")
    public IndividualCustomerResponse getById(Long id) {
        IndividualCustomer customer = repository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException(Messages.INDIVIDUAL_CUSTOMER_NOT_FOUND));
        return mapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.INDIVIDUAL_CUSTOMER_LIST)
    public List<IndividualCustomerResponse> getAll() {
        return repository.findAllByIsActiveTrue().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @Caching(
            put = @CachePut(value = CacheNames.INDIVIDUAL_CUSTOMERS, key = "#request.id"),
            evict = @CacheEvict(value = CacheNames.INDIVIDUAL_CUSTOMER_LIST, allEntries = true)
    )
    public IndividualCustomerResponse update(UpdateIndividualCustomerRequest request) {
        rules.checkIfIndividualCustomerExists(request.id());
        rules.checkIfBirthDateValid(request.birthDate());

        IndividualCustomer customer = repository.findByIdAndIsActiveTrue(request.id())
                .orElseThrow(() -> new BusinessException(Messages.INDIVIDUAL_CUSTOMER_NOT_FOUND));

        // Skaler alanları güncelle
        customer.setFirstName(request.firstName());
        customer.setSecondName(request.secondName());
        customer.setLastName(request.lastName());
        customer.setBirthDate(request.birthDate());
        customer.setFatherName(request.fatherName());
        customer.setMotherName(request.motherName());
        customer.setNationalityId(request.nationalityId());
        customer.setGenderType(request.genderType());

        // İç içe koleksiyonlar gönderildiyse tamamen değiştir (orphanRemoval eskileri siler)
        if (request.contactInfos() != null) {
            customer.getContactInfos().clear();
            applyContactInfos(customer, request.contactInfos());
        }
        if (request.addresses() != null) {
            customer.getAddresses().clear();
            applyAddresses(customer, request.addresses());
        }

        IndividualCustomer saved = repository.save(customer);
        publishEvent(saved, CustomerEvents.CUSTOMER_UPDATED);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.INDIVIDUAL_CUSTOMERS, key = "#id"),
            @CacheEvict(value = CacheNames.INDIVIDUAL_CUSTOMER_LIST, allEntries = true)
    })
    public void delete(Long id) {
        rules.checkIfIndividualCustomerExists(id);

        IndividualCustomer customer = repository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException(Messages.INDIVIDUAL_CUSTOMER_NOT_FOUND));

        // Soft-delete: fiziksel silme yok; pasifleştir ve silinme zamanını işaretle.
        customer.setIsActive(false);
        customer.setDeletedDate(LocalDateTime.now());
        repository.save(customer);

        publishEvent(customer, CustomerEvents.CUSTOMER_DELETED);
    }

    // ------------------------------------------------------------------ yardımcılar

    private void applyContactInfos(IndividualCustomer customer, List<CreateContactInfoRequest> requests) {
        if (requests == null) {
            return;
        }
        requests.forEach(req -> customer.addContactInfo(mapper.toContactInfo(req)));
    }

    private void applyAddresses(IndividualCustomer customer, List<CreateAddressRequest> requests) {
        if (requests == null) {
            return;
        }
        requests.forEach(req -> customer.addAddress(mapper.toAddress(req)));
    }

    private List<String> extractEmails(List<CreateContactInfoRequest> contactInfos) {
        if (contactInfos == null) {
            return List.of();
        }
        return contactInfos.stream()
                .map(CreateContactInfoRequest::email)
                .filter(Objects::nonNull)
                .toList();
    }

    private void publishEvent(IndividualCustomer customer, String eventType) {
        CustomerEventPayload payload = new CustomerEventPayload(
                customer.getId(), customer.getFirstName(), customer.getLastName(), LocalDateTime.now());
        outboxService.publish(
                CustomerEvents.AGGREGATE_TYPE, String.valueOf(customer.getId()), eventType, payload);
    }
}
