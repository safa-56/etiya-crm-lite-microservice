package com.etiya.customerservice.business.concretes;

import com.etiya.customerservice.business.abstracts.AddressService;
import com.etiya.customerservice.business.abstracts.CustomerContactInfoService;
import com.etiya.customerservice.business.abstracts.IndividualCustomerService;
import com.etiya.customerservice.business.abstracts.OutboxService;
import com.etiya.customerservice.business.abstracts.PartyRoleService;
import com.etiya.customerservice.business.abstracts.ReferenceDataService;
import com.etiya.customerservice.business.constants.CustomerEvents;
import com.etiya.customerservice.business.constants.Messages;
import com.etiya.customerservice.business.constants.PartyReferenceCodes;
import com.etiya.customerservice.business.dtos.events.CustomerEventPayload;
import com.etiya.customerservice.business.dtos.requests.CreateAddressRequest;
import com.etiya.customerservice.business.dtos.requests.CreateContactInfoRequest;
import com.etiya.customerservice.business.dtos.requests.CreateIndividualCustomerRequest;
import com.etiya.customerservice.business.dtos.requests.UpdateIndividualCustomerRequest;
import com.etiya.customerservice.business.dtos.responses.AddressResponse;
import com.etiya.customerservice.business.dtos.responses.ContactInfoResponse;
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

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class IndividualCustomerManager implements IndividualCustomerService {

    private final IndividualCustomerRepository repository;
    private final CustomerContactInfoRepository contactInfoRepository;
    private final AddressRepository addressRepository;
    private final IndividualCustomerMapper customerMapper;
    private final IndividualCustomerBusinessRules rules;
    private final OutboxService outboxService;
    private final PartyRoleService partyRoleService;
    private final ReferenceDataService referenceDataService;

    // Çocuk kayıtlar (iletişim bilgisi/adres) kendi servisleri üzerinden oluşturulur.
    // Bu servisler IndividualCustomerService'e bağımlı olduğundan oluşan döngüsel
    // bağımlılığı kırmak için @Lazy ile enjekte edilir (lombok.config bu anotasyonu
    // üretilen constructor parametresine kopyalar).
    @Lazy
    private final CustomerContactInfoService contactInfoService;
    @Lazy
    private final AddressService addressService;

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.INDIVIDUAL_CUSTOMER_LIST, allEntries = true)
    public IndividualCustomerResponse add(CreateIndividualCustomerRequest request) {
        rules.checkIfNationalityIdAlreadyExists(request.nationalityId());
        rules.checkIfEmailsAlreadyExist(extractEmails(List.of(request.contactInfo())));

        IndividualCustomer customer = customerMapper.toEntity(request);

        customer.setPartyRole(partyRoleService.createCustomerRoleForIndividual());
        customer.setGeneralStatus(referenceDataService.getStatus(
                PartyReferenceCodes.ENTITY_INDIVIDUAL, PartyReferenceCodes.STATUS_ACTIVE_CODE));

        IndividualCustomer saved = repository.save(customer);

        // Çocuk kayıtları kendi servisleri üzerinden oluştur (müşteri id'siyle bağla).
        ContactInfoResponse contact = contactInfoService.add(
                withCustomerId(request.contactInfo(), saved.getId()));
        AddressResponse address = addressService.add(
                withCustomerId(request.address(), saved.getId()));
        
        // CustomerCreated olayını servislerden dönen verilerle yayınla: yeni müşterinin
        // in-memory koleksiyonları bu noktada dolu olmadığından entity'den okunamaz.
        publishEvent(saved, CustomerEvents.CUSTOMER_CREATED, contact.mobilPhone(),
                List.of(new CustomerEventPayload.AddressPayload(
                        address.id(), address.city(), address.street(),
                        address.houseNumber(), address.addressDescription(), address.isPrimary())));

        return customerMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.INDIVIDUAL_CUSTOMERS, key = "#id")
    public IndividualCustomerResponse getById(Long id) {
        IndividualCustomer customer = repository.findByIdAndDeletedDateIsNull(id)
                .orElseThrow(() -> new BusinessException(Messages.INDIVIDUAL_CUSTOMER_NOT_FOUND));
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.INDIVIDUAL_CUSTOMER_LIST)
    public List<IndividualCustomerResponse> getAll() {
        return repository.findAllByDeletedDateIsNull().stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @Caching(
            put = @CachePut(value = CacheNames.INDIVIDUAL_CUSTOMERS, key = "#id"),
            evict = @CacheEvict(value = CacheNames.INDIVIDUAL_CUSTOMER_LIST, allEntries = true)
    )
    public IndividualCustomerResponse update(Long id, UpdateIndividualCustomerRequest request) {
        rules.checkIfNationalityIdBelongsToAnotherCustomer(request.nationalityId(), id);

        IndividualCustomer customer = rules.checkIndividualCustomerIsExists(id);

        // customer.setFirstName(request.firstName());
        // customer.setSecondName(request.secondName());
        // customer.setLastName(request.lastName());
        // customer.setBirthDate(request.birthDate());
        // customer.setFatherName(request.fatherName());
        // customer.setMotherName(request.motherName());
        // customer.setNationalityId(request.nationalityId());
        // customer.setGenderType(request.genderType());

        customerMapper.updateEntity(customer, request);

        IndividualCustomer saved = repository.save(customer);
        publishEvent(saved, CustomerEvents.CUSTOMER_UPDATED);

        return customerMapper.toResponse(saved);
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
        customer.setGeneralStatus(referenceDataService.getStatus(
                PartyReferenceCodes.ENTITY_INDIVIDUAL, PartyReferenceCodes.STATUS_DELETED_CODE));
        customer.setDeletedDate(LocalDateTime.now());
        
        repository.save(customer);

        deactivateContactInfos(customer);
        deactivateAddresses(customer);

        // Zincirin üst halkaları da pasifleşir (party rolü + party).
        partyRoleService.deactivate(customer.getPartyRole());

        publishEvent(customer, CustomerEvents.CUSTOMER_DELETED);
    }

    @Override
    public IndividualCustomer getIndividualCustomerById(Long id) {
        IndividualCustomer individualCustomer = rules.checkIndividualCustomerIsExists(id);
        return individualCustomer;
    }

    // ------------------------------------------------------------------ yardımcılar

    /** Nested create isteğini, oluşturulan müşterinin id'siyle yeniden kurar (servise geçmek için). */
    private CreateContactInfoRequest withCustomerId(CreateContactInfoRequest request, Long customerId) {
        return new CreateContactInfoRequest(customerId, request.email(),
                request.homePhone(), request.mobilPhone(), request.fax());
    }

    /** Nested create isteğini, oluşturulan müşterinin id'siyle yeniden kurar (servise geçmek için). */
    private CreateAddressRequest withCustomerId(CreateAddressRequest request, Long customerId) {
        return new CreateAddressRequest(customerId, request.city(), request.street(),
                request.houseNumber(), request.addressDescription(), request.isPrimary());
    }

    /** Müşterinin mevcut iletişim bilgilerini soft-delete ile pasifleştirir. */
    private void deactivateContactInfos(IndividualCustomer customer) {
        List<CustomerContactInfo> contactInfos = customer.getContactInfos();
        if (contactInfos.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        var deletedStatus = referenceDataService.getStatus(
                PartyReferenceCodes.ENTITY_CONTACT_INFO, PartyReferenceCodes.STATUS_DELETED_CODE);
        contactInfos.forEach(contactInfo -> {
            contactInfo.setGeneralStatus(deletedStatus);
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
        var deletedStatus = referenceDataService.getStatus(
                PartyReferenceCodes.ENTITY_ADDRESS, PartyReferenceCodes.STATUS_DELETED_CODE);
        addresses.forEach(address -> {
            address.setGeneralStatus(deletedStatus);
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
        publishEvent(customer, eventType, primaryGsmNumber(customer), toAddressPayloads(customer));
    }

    private void publishEvent(IndividualCustomer customer, String eventType, String gsmNumber,
                              List<CustomerEventPayload.AddressPayload> addresses) {
        CustomerEventPayload payload = new CustomerEventPayload(
                customer.getId(), customer.getFirstName(), customer.getSecondName(),
                customer.getLastName(), customer.getNationalityId(), gsmNumber,
                CustomerEvents.ROLE_B2C, eventType, addresses, LocalDateTime.now());
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
                .filter(contactInfo -> contactInfo.getDeletedDate() == null)
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
                .filter(address -> address.getDeletedDate() == null)
                .map(address -> new CustomerEventPayload.AddressPayload(
                        address.getId(), address.getCity(), address.getStreet(),
                        address.getHouseNumber(), address.getAddressDescription(),
                        address.getIsPrimary()))
                .toList();
    }
}
