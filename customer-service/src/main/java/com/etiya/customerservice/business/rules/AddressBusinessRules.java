package com.etiya.customerservice.business.rules;

import com.etiya.customerservice.business.constants.Messages;
import com.etiya.customerservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.customerservice.dataAccess.AddressRepository;
import com.etiya.customerservice.dataAccess.CustomerRepository;
import org.springframework.stereotype.Service;

/**
 * Adres iş kuralları.
 *
 * <p>Veri/durum bağımlı kontrolleri toplar; {@link AddressManager}'a inject
 * edilir. Kural ihlallerinde {@link BusinessException} fırlatılır (mesajlar
 * {@link Messages} sabitlerinden).
 */
@Service
public class AddressBusinessRules {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    public AddressBusinessRules(AddressRepository addressRepository,
                                CustomerRepository customerRepository) {
        this.addressRepository = addressRepository;
        this.customerRepository = customerRepository;
    }

    /** Aktif bir adres id ile var olmalı; yoksa iş hatası fırlatılır. */
    public void checkIfAddressExists(Long id) {
        if (!addressRepository.existsByIdAndIsActiveTrue(id)) {
            throw new BusinessException(Messages.ADDRESS_NOT_FOUND);
        }
    }

    /** Adresin bağlanacağı aktif müşteri id ile var olmalı; yoksa iş hatası fırlatılır. */
    public void checkIfCustomerExists(Long customerId) {
        if (customerId == null || !customerRepository.existsByIdAndIsActiveTrue(customerId)) {
            throw new BusinessException(Messages.CUSTOMER_NOT_FOUND);
        }
    }
}
