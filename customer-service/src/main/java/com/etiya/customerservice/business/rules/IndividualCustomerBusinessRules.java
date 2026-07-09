package com.etiya.customerservice.business.rules;

import com.etiya.customerservice.business.constants.Messages;
import com.etiya.customerservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.customerservice.dataAccess.CustomerContactInfoRepository;
import com.etiya.customerservice.dataAccess.IndividualCustomerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Bireysel müşteri iş kuralları.
 *
 * <p>İş katmanındaki kontrolleri (validation'dan farklı olarak veri/durum
 * bağımlı kurallar) burada toplar. İlgili business sınıfına (manager) inject
 * edilir; manager kuralları çağırarak akışı yönetir. Kural ihlallerinde
 * {@link BusinessException} fırlatılır (mesajlar {@link Messages} sabitlerinden).
 */
@Service
public class IndividualCustomerBusinessRules {

    private final IndividualCustomerRepository individualCustomerRepository;
    private final CustomerContactInfoRepository contactInfoRepository;

    public IndividualCustomerBusinessRules(IndividualCustomerRepository individualCustomerRepository,
                                           CustomerContactInfoRepository contactInfoRepository) {
        this.individualCustomerRepository = individualCustomerRepository;
        this.contactInfoRepository = contactInfoRepository;
    }

    /** Aktif bir bireysel müşteri id ile var olmalı; yoksa iş hatası fırlatılır. */
    public void checkIfIndividualCustomerExists(Long id) {
        if (!individualCustomerRepository.existsByIdAndIsActiveTrue(id)) {
            throw new BusinessException(Messages.INDIVIDUAL_CUSTOMER_NOT_FOUND);
        }
    }

    /**
     * Yeni müşteri oluşturmada TC kimlik numarası (Nationality ID) tekilliğini
     * doğrular: aynı numaraya sahip aktif bir müşteri varsa iş hatası fırlatılır
     * (FR-003 ACC-07/08).
     */
    public void checkIfNationalityIdAlreadyExists(String nationalityId) {
        if (nationalityId != null
                && individualCustomerRepository.existsByNationalityIdAndIsActiveTrue(nationalityId)) {
            throw new BusinessException(Messages.NATIONALITY_ID_ALREADY_EXISTS);
        }
    }

    /**
     * Güncellemede TC kimlik numarasının, güncellenen müşteri dışında başka bir
     * aktif müşteriye ait olmadığını doğrular. Müşterinin kendi mevcut numarası
     * hatayı tetiklemez (FR-004 ACC-07/08).
     */
    public void checkIfNationalityIdBelongsToAnotherCustomer(String nationalityId, Long customerId) {
        if (nationalityId != null && customerId != null
                && individualCustomerRepository
                        .existsByNationalityIdAndIdNotAndIsActiveTrue(nationalityId, customerId)) {
            throw new BusinessException(Messages.NATIONALITY_ID_ALREADY_EXISTS);
        }
    }

    /** Verilen e-postalardan herhangi biri aktif bir kayıtta zaten kullanılıyorsa hata verir. */
    public void checkIfEmailsAlreadyExist(List<String> emails) {
        if (emails == null) {
            return;
        }
        for (String email : emails) {
            if (email != null && !email.isBlank()
                    && contactInfoRepository.existsByEmailIgnoreCaseAndIsActiveTrue(email)) {
                throw new BusinessException(Messages.EMAIL_ALREADY_EXISTS);
            }
        }
    }
}
