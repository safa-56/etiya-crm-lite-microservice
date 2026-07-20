package com.etiya.customerservice.business.rules;

import com.etiya.customerservice.business.constants.Messages;
import com.etiya.customerservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.customerservice.dataAccess.AddressRepository;
import com.etiya.customerservice.dataAccess.CustomerRepository;
import com.etiya.customerservice.entities.Address;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    public Address checkAddressIfExists(Long id) {
        Optional<Address> existsAddress = addressRepository.findByIdAndDeletedDateIsNull(id);
        if (existsAddress.isEmpty()) {
            throw new BusinessException(Messages.ADDRESS_NOT_FOUND);
        }
        return existsAddress.get();
    }

    /** Adresin bağlanacağı aktif müşteri id ile var olmalı; yoksa iş hatası fırlatılır. */
    public void checkIfCustomerExists(Long customerId) {
        if (customerId == null || !customerRepository.existsByIdAndDeletedDateIsNull(customerId)) {
            throw new BusinessException(Messages.CUSTOMER_NOT_FOUND);
        }
    }

    /**
     * "Bir müşterinin en fazla bir birincil adresi olur" değişmezini uygular:
     * verilen müşterinin, {@code excludedAddressId} dışındaki mevcut birincil
     * adreslerinin {@code isPrimary} bayrağını kaldırır (FR-006 ACC-07).
     *
     * <p>Bir adres birincil yapılmadan (kaydedilmeden) hemen önce, aynı transaction
     * içinde çağrılır. Yeni oluşturmada henüz id olmadığından {@code excludedAddressId}
     * {@code null} geçilebilir.
     *
     * @param customerId        birincil adresleri düşürülecek müşteri
     * @param excludedAddressId hariç tutulacak adres (yeni birincil yapılan); yoksa null
     */
    public void demoteExistingPrimaryAddresses(Long customerId, Long excludedAddressId) {
        if (customerId == null) {
            return;
        }
        List<Address> toDemote = addressRepository
                .findByCustomer_IdAndIsPrimaryTrueAndDeletedDateIsNull(customerId).stream()
                .filter(address -> excludedAddressId == null
                        || !excludedAddressId.equals(address.getId()))
                .filter(address -> Boolean.TRUE.equals(address.getIsPrimary()))
                .peek(address -> address.setIsPrimary(false))
                .toList();
        if (!toDemote.isEmpty()) {
            addressRepository.saveAll(toDemote);
        }
    }
}
