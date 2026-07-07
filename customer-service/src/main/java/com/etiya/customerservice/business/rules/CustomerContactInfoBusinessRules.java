package com.etiya.customerservice.business.rules;

import com.etiya.customerservice.business.constants.Messages;
import com.etiya.customerservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.customerservice.dataAccess.CustomerContactInfoRepository;
import com.etiya.customerservice.dataAccess.CustomerRepository;
import org.springframework.stereotype.Service;

/**
 * Müşteri iletişim bilgisi iş kuralları.
 *
 * <p>Veri/durum bağımlı kontrolleri toplar; {@link CustomerContactInfoManager}'a
 * inject edilir. Kural ihlallerinde {@link BusinessException} fırlatılır
 * (mesajlar {@link Messages} sabitlerinden).
 */
@Service
public class CustomerContactInfoBusinessRules {

    private final CustomerContactInfoRepository contactInfoRepository;
    private final CustomerRepository customerRepository;

    public CustomerContactInfoBusinessRules(CustomerContactInfoRepository contactInfoRepository,
                                            CustomerRepository customerRepository) {
        this.contactInfoRepository = contactInfoRepository;
        this.customerRepository = customerRepository;
    }

    /** Aktif bir iletişim bilgisi id ile var olmalı; yoksa iş hatası fırlatılır. */
    public void checkIfContactInfoExists(Long id) {
        if (!contactInfoRepository.existsByIdAndIsActiveTrue(id)) {
            throw new BusinessException(Messages.CONTACT_INFO_NOT_FOUND);
        }
    }

    /** İletişim bilgisinin bağlanacağı aktif müşteri id ile var olmalı; yoksa iş hatası fırlatılır. */
    public void checkIfCustomerExists(Long customerId) {
        if (customerId == null || !customerRepository.existsByIdAndIsActiveTrue(customerId)) {
            throw new BusinessException(Messages.CUSTOMER_NOT_FOUND);
        }
    }

    /** Verilen e-posta aktif bir kayıtta zaten kullanılıyorsa hata verir. */
    public void checkIfEmailAlreadyExists(String email) {
        if (email != null && !email.isBlank()
                && contactInfoRepository.existsByEmailIgnoreCaseAndIsActiveTrue(email)) {
            throw new BusinessException(Messages.EMAIL_ALREADY_EXISTS);
        }
    }
}
