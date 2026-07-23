package com.etiya.customerservice.business.concretes;

import com.etiya.customerservice.business.abstracts.CustomerContactInfoService;
import com.etiya.customerservice.business.abstracts.ReferenceDataService;
import com.etiya.customerservice.business.constants.PartyReferenceCodes;
import com.etiya.customerservice.business.dtos.requests.CreateContactInfoRequest;
import com.etiya.customerservice.business.dtos.requests.UpdateContactInfoRequest;
import com.etiya.customerservice.business.dtos.responses.ContactInfoResponse;
import com.etiya.customerservice.business.mappers.CustomerContactInfoMapper;
import com.etiya.customerservice.business.rules.CustomerContactInfoBusinessRules;
import com.etiya.customerservice.dataAccess.CustomerContactInfoRepository;
import com.etiya.customerservice.entities.Customer;
import com.etiya.customerservice.entities.CustomerContactInfo;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Müşteri iletişim bilgisi iş servisi uygulaması (business concrete).
 *
 * <p>CRUD işlemlerini yürütür; iş kurallarını
 * {@link CustomerContactInfoBusinessRules}'a delege eder, DTO/entity dönüşümünü
 * {@link CustomerContactInfoMapper} ile yapar ve silme işleminde soft-delete uygular.
 *
 * <p>Not: İletişim bilgisi, {@code Customer} agregasının bir çocuğudur; müşteri
 * cache'i ({@code individualCustomers}) iletişim bilgilerini gömülü tuttuğundan,
 * tutarsızlık (stale) riskini önlemek için burada bağımsız bir cache kullanılmaz.
 */
@Service
@RequiredArgsConstructor
public class CustomerContactInfoManager implements CustomerContactInfoService {

    private final CustomerContactInfoRepository repository;
    private final CustomerContactInfoMapper mapper;
    private final CustomerContactInfoBusinessRules rules;
    private final ReferenceDataService referenceDataService;

    @Override
    @Transactional
    public ContactInfoResponse add(CreateContactInfoRequest request) {
        Customer customer = rules.checkIfCustomerExists(request.customerId());
        rules.checkIfEmailAlreadyExists(request.email());

        CustomerContactInfo contactInfo = mapper.toEntity(request);

        contactInfo.setCustomer(customer);
        contactInfo.setGeneralStatus(referenceDataService.getStatus(
                PartyReferenceCodes.ENTITY_CONTACT_INFO, PartyReferenceCodes.STATUS_ACTIVE_CODE));

        CustomerContactInfo saved = repository.save(contactInfo);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ContactInfoResponse getById(Long id) {
        CustomerContactInfo customerContactInfo = rules.checkContactInfoIfExists(id);
        return mapper.toResponse(customerContactInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactInfoResponse> getAll() {
        return repository.findAllByDeletedDateIsNull().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ContactInfoResponse update(Long id, UpdateContactInfoRequest request) {

        CustomerContactInfo contactInfo = rules.checkContactInfoIfExists(id);

        if (!Objects.equals(request.email(), contactInfo.getEmail())) {
            rules.checkIfEmailAlreadyExists(request.email());
        }

        // contactInfo.setEmail(request.email());
        // contactInfo.setHomePhone(request.homePhone());
        // contactInfo.setMobilPhone(request.mobilPhone());
        // contactInfo.setFax(request.fax());

        mapper.updateEntity(contactInfo, request);

        return mapper.toResponse(repository.save(contactInfo));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        CustomerContactInfo contactInfo = rules.checkContactInfoIfExists(id);

        contactInfo.setGeneralStatus(referenceDataService.getStatus(
                PartyReferenceCodes.ENTITY_CONTACT_INFO, PartyReferenceCodes.STATUS_DELETED_CODE));
        
        contactInfo.setDeletedDate(LocalDateTime.now());
        repository.save(contactInfo);
    }
}
