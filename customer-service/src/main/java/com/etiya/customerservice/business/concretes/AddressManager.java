package com.etiya.customerservice.business.concretes;

import com.etiya.customerservice.business.abstracts.AddressService;
import com.etiya.customerservice.business.abstracts.IndividualCustomerService;
import com.etiya.customerservice.business.abstracts.OutboxService;
import com.etiya.customerservice.business.abstracts.ReferenceDataService;
import com.etiya.customerservice.business.constants.CustomerEvents;
import com.etiya.customerservice.business.constants.PartyReferenceCodes;
import com.etiya.customerservice.business.constants.Messages;
import com.etiya.customerservice.business.dtos.events.CustomerEventPayload;
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
    //private final CustomerRepository customerRepository;
    private IndividualCustomerService individualCustomerService;
    private final AddressMapper mapper;
    private final AddressBusinessRules rules;
    private final OutboxService outboxService;
    private final ReferenceDataService referenceDataService;

    public AddressManager(AddressRepository repository,
                          IndividualCustomerService individualCustomerService,
                          AddressMapper mapper,
                          AddressBusinessRules rules,
                          OutboxService outboxService,
                          ReferenceDataService referenceDataService) {
        this.repository = repository;
        this.individualCustomerService = individualCustomerService;
        this.mapper = mapper;
        this.rules = rules;
        this.outboxService = outboxService;
        this.referenceDataService = referenceDataService;
    }

    @Override
    @Transactional
    public AddressResponse add(CreateAddressRequest request) {
        // --- iş kuralları ---
        rules.checkIfCustomerExists(request.customerId());

        // --- dönüşüm + ilişki kurulumu ---
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

        // Adres kümesi değişti: account-service projeksiyonunu tazelemek için
        // müşterinin güncel adresleriyle bir CustomerUpdated olayı yayınla.
        publishCustomerAddressSnapshot(customer.getId());

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

        // Skaler alanları güncelle (müşteri ilişkisi değiştirilmez).
        address.setCity(request.city());
        address.setStreet(request.street());
        address.setHouseNumber(request.houseNumber());
        address.setAddressDescription(request.addressDescription());
        if (Boolean.TRUE.equals(request.isPrimary())) {
            // Bu adres birincil yapılıyorsa, aynı müşterinin daha önce birincil
            // işaretlenmiş adresinin bayrağını kaldır (FR-006 ACC-07).
            rules.demoteExistingPrimaryAddresses(address.getCustomer().getId(), address.getId());
            address.setIsPrimary(true);
        } else if (request.isPrimary() != null) {
            address.setIsPrimary(request.isPrimary());
        }

        Address saved = repository.save(address);
        publishCustomerAddressSnapshot(saved.getCustomer().getId());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Address address = rules.checkAddressIfExists(id);
        Long customerId = address.getCustomer().getId();

        // Soft-delete: fiziksel silme yok; durumu DEL yap ve silinme zamanını işaretle.
        address.setGeneralStatus(referenceDataService.getStatus(
                PartyReferenceCodes.ENTITY_ADDRESS, PartyReferenceCodes.STATUS_DELETED_CODE));
        address.setDeletedDate(LocalDateTime.now());
        repository.save(address);

        publishCustomerAddressSnapshot(customerId);
    }

    // ------------------------------------------------------------------ yardımcılar

//    private Address findActiveAddress(Long id) {
//        return repository.findByIdAndDeletedDateIsNull(id)
//                .orElseThrow(() -> new BusinessException(Messages.ADDRESS_NOT_FOUND));
//    }

    /**
     * Müşterinin güncel (aktif) adres kümesini bir {@code CustomerUpdated} olayı
     * olarak outbox'a yazar. Böylece standalone adres ekleme/güncelleme/silme
     * işlemleri de account-service'in yerel müşteri-adres projeksiyonuna yansır.
     *
     * <p>Ad/soyad/TCKN/GSM bu bağlamda bilinmediğinden {@code null} gönderilir;
     * tüketici bunları yalnızca dolu geldiğinde günceller, aksi halde mevcut
     * değeri korur (null-safe upsert).
     */
    private void publishCustomerAddressSnapshot(Long customerId) {
        List<CustomerEventPayload.AddressPayload> addresses =
                repository.findByCustomer_IdAndDeletedDateIsNull(customerId).stream()
                        .map(address -> new CustomerEventPayload.AddressPayload(
                                address.getId(), address.getCity(), address.getStreet(),
                                address.getHouseNumber(), address.getAddressDescription(),
                                address.getIsPrimary()))
                        .toList();

        CustomerEventPayload payload = new CustomerEventPayload(
                customerId, null, null, null, null, null, null,
                CustomerEvents.CUSTOMER_UPDATED, addresses, LocalDateTime.now());
        outboxService.publish(
                CustomerEvents.AGGREGATE_TYPE, String.valueOf(customerId),
                CustomerEvents.CUSTOMER_UPDATED, payload);
    }
}
