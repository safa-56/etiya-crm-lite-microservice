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
import com.etiya.customerservice.dataAccess.AddressRepository;
import com.etiya.customerservice.dataAccess.CustomerContactInfoRepository;
import com.etiya.customerservice.dataAccess.IndividualCustomerRepository;
import com.etiya.customerservice.entities.Address;
import com.etiya.customerservice.entities.CustomerContactInfo;
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

@Service
public class IndividualCustomerManager implements IndividualCustomerService {

    private final IndividualCustomerRepository repository;
    private final CustomerContactInfoRepository contactInfoRepository;
    private final AddressRepository addressRepository;
    private final IndividualCustomerMapper mapper;
    private final IndividualCustomerBusinessRules rules;
    private final OutboxService outboxService;

    public IndividualCustomerManager(IndividualCustomerRepository repository,
                                     CustomerContactInfoRepository contactInfoRepository,
                                     AddressRepository addressRepository,
                                     IndividualCustomerMapper mapper,
                                     IndividualCustomerBusinessRules rules,
                                     OutboxService outboxService) {
        this.repository = repository;
        this.contactInfoRepository = contactInfoRepository;
        this.addressRepository = addressRepository;
        this.mapper = mapper;
        this.rules = rules;
        this.outboxService = outboxService;
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.INDIVIDUAL_CUSTOMER_LIST, allEntries = true)
    public IndividualCustomerResponse add(CreateIndividualCustomerRequest request) {
        // --- iş kuralları ---
        rules.checkIfNationalityIdAlreadyExists(request.nationalityId());
        rules.checkIfEmailsAlreadyExist(extractEmails(request.contactInfos()));

        // --- müşteriyi tek başına persist et (cascade yok) ---
        IndividualCustomer saved = repository.save(mapper.toEntity(request));

        // --- çocuk kayıtları açıkça persist et (kendi repository'leri üzerinden) ---
        persistContactInfos(saved, request.contactInfos());
        persistAddresses(saved, request.addresses());

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
            put = @CachePut(value = CacheNames.INDIVIDUAL_CUSTOMERS, key = "#id"),
            evict = @CacheEvict(value = CacheNames.INDIVIDUAL_CUSTOMER_LIST, allEntries = true)
    )
    public IndividualCustomerResponse update(Long id, UpdateIndividualCustomerRequest request) {
        // Nationality ID başka bir müşteriye ait olmamalı (kendisi hariç).
        rules.checkIfNationalityIdBelongsToAnotherCustomer(request.nationalityId(), id);

        IndividualCustomer customer = rules.checkIndividualCustomerIsExists(id);

        // Skaler alanları güncelle
        customer.setFirstName(request.firstName());
        customer.setSecondName(request.secondName());
        customer.setLastName(request.lastName());
        customer.setBirthDate(request.birthDate());
        customer.setFatherName(request.fatherName());
        customer.setMotherName(request.motherName());
        customer.setNationalityId(request.nationalityId());
        customer.setGenderType(request.genderType());

        // İç içe koleksiyonlar gönderildiyse tamamen değiştir: eskileri pasifleştir
        // (soft-delete), yenilerini açıkça persist et (cascade/orphanRemoval yok).
        if (request.contactInfos() != null) {
            deactivateContactInfos(customer);
            persistContactInfos(customer, request.contactInfos());
        }
        if (request.addresses() != null) {
            deactivateAddresses(customer);
            persistAddresses(customer, request.addresses());
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

        IndividualCustomer customer = rules.checkIndividualCustomerIsExists(id);

        // Soft-delete: fiziksel silme yok; pasifleştir ve silinme zamanını işaretle.
        // Çocuk kayıtlar (adresler + iletişim bilgileri) de aynı anda pasifleştirilir.
        customer.setIsActive(false);
        customer.setDeletedDate(LocalDateTime.now());
        repository.save(customer);

        deactivateContactInfos(customer);
        deactivateAddresses(customer);

        publishEvent(customer, CustomerEvents.CUSTOMER_DELETED);
    }

    @Override
    public IndividualCustomer getIndividualCustomerById(Long id) {
        IndividualCustomer individualCustomer = rules.checkIndividualCustomerIsExists(id);
        return individualCustomer;
    }

    // ------------------------------------------------------------------ yardımcılar

    /** İletişim bilgilerini açıkça persist eder (cascade yok) ve müşteriye bağlar. */
    private void persistContactInfos(IndividualCustomer customer, List<CreateContactInfoRequest> requests) {
        if (requests == null) {
            return;
        }
        requests.forEach(req -> {
            CustomerContactInfo contactInfo = mapper.toContactInfo(req);
            customer.addContactInfo(contactInfo);     // iki yönlü ilişki + koleksiyon
            contactInfoRepository.save(contactInfo);  // açıkça persist
        });
    }

    /** Adresleri açıkça persist eder (cascade yok) ve müşteriye bağlar. */
    private void persistAddresses(IndividualCustomer customer, List<CreateAddressRequest> requests) {
        if (requests == null) {
            return;
        }
        requests.forEach(req -> {
            Address address = mapper.toAddress(req);
            customer.addAddress(address);       // iki yönlü ilişki + koleksiyon
            addressRepository.save(address);    // açıkça persist
        });
    }

    /** Müşterinin mevcut iletişim bilgilerini soft-delete ile pasifleştirir. */
    private void deactivateContactInfos(IndividualCustomer customer) {
        List<CustomerContactInfo> contactInfos = customer.getContactInfos();
        if (contactInfos.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        contactInfos.forEach(contactInfo -> {
            contactInfo.setIsActive(false);
            contactInfo.setDeletedDate(now);
        });
        contactInfoRepository.saveAll(contactInfos);
        contactInfos.clear(); // in-memory koleksiyonu boşalt (yeni gelenler eklenecek)
    }

    /** Müşterinin mevcut adreslerini soft-delete ile pasifleştirir. */
    private void deactivateAddresses(IndividualCustomer customer) {
        List<Address> addresses = customer.getAddresses();
        if (addresses.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        addresses.forEach(address -> {
            address.setIsActive(false);
            address.setDeletedDate(now);
        });
        addressRepository.saveAll(addresses);
        addresses.clear(); // in-memory koleksiyonu boşalt (yeni gelenler eklenecek)
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
                customer.getId(), customer.getFirstName(), customer.getSecondName(),
                customer.getLastName(), customer.getNationalityId(), primaryGsmNumber(customer),
                CustomerEvents.ROLE_B2C, eventType, toAddressPayloads(customer), LocalDateTime.now());
        outboxService.publish(
                CustomerEvents.AGGREGATE_TYPE, String.valueOf(customer.getId()), eventType, payload);
    }

    /**
     * Müşterinin birincil GSM numarasını döndürür (search read-model'i için).
     * İletişim bilgisinde ayrı bir "birincil" bayrağı olmadığından, ilk aktif
     * iletişim bilgisinin GSM'i ({@code mobilPhone}) birincil kabul edilir.
     */
    private String primaryGsmNumber(IndividualCustomer customer) {
        return customer.getContactInfos().stream()
                .filter(contactInfo -> Boolean.TRUE.equals(contactInfo.getIsActive()))
                .map(CustomerContactInfo::getMobilPhone)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * Müşterinin o an aktif olan adreslerini olay gövdesine dönüştürür. Silmede
     * adresler pasifleştirildiğinden (koleksiyon boşaltılır) liste boş döner;
     * account-service {@code CustomerDeleted} olayında zaten adresleri temizler.
     */
    private List<CustomerEventPayload.AddressPayload> toAddressPayloads(IndividualCustomer customer) {
        return customer.getAddresses().stream()
                .filter(address -> Boolean.TRUE.equals(address.getIsActive()))
                .map(address -> new CustomerEventPayload.AddressPayload(
                        address.getId(), address.getCity(), address.getStreet(),
                        address.getHouseNumber(), address.getAddressDescription(),
                        address.getIsPrimary()))
                .toList();
    }
}
