package com.etiya.customerservice.business.concretes;

import com.etiya.customerservice.business.abstracts.CustomerContactInfoService;
import com.etiya.customerservice.business.constants.Messages;
import com.etiya.customerservice.business.dtos.requests.CreateContactInfoRequest;
import com.etiya.customerservice.business.dtos.requests.UpdateContactInfoRequest;
import com.etiya.customerservice.business.dtos.responses.ContactInfoResponse;
import com.etiya.customerservice.business.mappers.CustomerContactInfoMapper;
import com.etiya.customerservice.business.rules.CustomerContactInfoBusinessRules;
import com.etiya.customerservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.customerservice.dataAccess.CustomerContactInfoRepository;
import com.etiya.customerservice.dataAccess.CustomerRepository;
import com.etiya.customerservice.entities.Customer;
import com.etiya.customerservice.entities.CustomerContactInfo;
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
public class CustomerContactInfoManager implements CustomerContactInfoService {

    private final CustomerContactInfoRepository repository;
    private final CustomerRepository customerRepository;
    private final CustomerContactInfoMapper mapper;
    private final CustomerContactInfoBusinessRules rules;

    public CustomerContactInfoManager(CustomerContactInfoRepository repository,
                                      CustomerRepository customerRepository,
                                      CustomerContactInfoMapper mapper,
                                      CustomerContactInfoBusinessRules rules) {
        this.repository = repository;
        this.customerRepository = customerRepository;
        this.mapper = mapper;
        this.rules = rules;
    }

    @Override
    @Transactional
    public ContactInfoResponse add(CreateContactInfoRequest request) {
        // --- iş kuralları ---
        rules.checkIfCustomerExists(request.customerId());
        rules.checkIfEmailAlreadyExists(request.email());

        // --- dönüşüm + ilişki kurulumu ---
        Customer customer = customerRepository.findByIdAndIsActiveTrue(request.customerId())
                .orElseThrow(() -> new BusinessException(Messages.CUSTOMER_NOT_FOUND));

        CustomerContactInfo contactInfo = mapper.toEntity(request);
        contactInfo.setCustomer(customer);

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
        return repository.findAllByIsActiveTrue().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ContactInfoResponse update(Long id, UpdateContactInfoRequest request) {

        CustomerContactInfo contactInfo = rules.checkContactInfoIfExists(id);

        // E-posta değişiyorsa benzersizliği tekrar doğrula.
        if (request.email() != null && !Objects.equals(request.email(), contactInfo.getEmail())) {
            rules.checkIfEmailAlreadyExists(request.email());
        }

        // Skaler alanları güncelle (müşteri ilişkisi değiştirilmez).
        contactInfo.setEmail(request.email());
        contactInfo.setHomePhone(request.homePhone());
        contactInfo.setMobilPhone(request.mobilPhone());
        contactInfo.setFax(request.fax());

        return mapper.toResponse(repository.save(contactInfo));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        CustomerContactInfo contactInfo = rules.checkContactInfoIfExists(id);

        // Soft-delete: fiziksel silme yok; pasifleştir ve silinme zamanını işaretle.
        contactInfo.setIsActive(false);
        contactInfo.setDeletedDate(LocalDateTime.now());
        repository.save(contactInfo);
    }

    // ------------------------------------------------------------------ yardımcılar

//    private CustomerContactInfo findActiveContactInfo(Long id) {
//        return repository.findByIdAndIsActiveTrue(id)
//                .orElseThrow(() -> new BusinessException(Messages.CONTACT_INFO_NOT_FOUND));
//    }
}
