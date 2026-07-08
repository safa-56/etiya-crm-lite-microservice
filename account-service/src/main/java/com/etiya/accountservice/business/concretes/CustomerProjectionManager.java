package com.etiya.accountservice.business.concretes;

import com.etiya.accountservice.business.abstracts.CustomerProjectionService;
import com.etiya.accountservice.business.constants.CustomerEvents;
import com.etiya.accountservice.business.dtos.events.CustomerEventPayload;
import com.etiya.accountservice.dataAccess.CustomerAddressProjectionRepository;
import com.etiya.accountservice.dataAccess.CustomerProjectionRepository;
import com.etiya.accountservice.entities.projection.CustomerAddressProjection;
import com.etiya.accountservice.entities.projection.CustomerProjection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * {@link CustomerProjectionService} uygulaması.
 *
 * <p>customer-service olaylarına göre yerel müşteri ve adres projeksiyonunu
 * günceller. Çağıran (Inbox) transaction'ı içinde çalışır; böylece projeksiyon
 * güncellemesi ile inbox kaydı atomik olur (duplicate consume'a karşı korumalı).
 *
 * <p>Davranış:
 * <ul>
 *   <li>{@code CustomerCreated}/{@code CustomerUpdated}: müşteriyi upsert eder ve
 *       adres kümesini tam olarak yeniden yazar (sil + ekle). Ad/soyad yalnızca
 *       olayda dolu geldiğinde güncellenir (standalone adres olayları bunları
 *       {@code null} taşır; mevcut değer korunur).</li>
 *   <li>{@code CustomerDeleted}: müşteriyi pasifleştirir ve adreslerini kaldırır
 *       (silinmiş müşteriye fatura hesabı adresi seçilemesin).</li>
 * </ul>
 */
@Service
public class CustomerProjectionManager implements CustomerProjectionService {

    private static final Logger log = LoggerFactory.getLogger(CustomerProjectionManager.class);

    private final CustomerProjectionRepository customerProjectionRepository;
    private final CustomerAddressProjectionRepository addressProjectionRepository;

    public CustomerProjectionManager(CustomerProjectionRepository customerProjectionRepository,
                                     CustomerAddressProjectionRepository addressProjectionRepository) {
        this.customerProjectionRepository = customerProjectionRepository;
        this.addressProjectionRepository = addressProjectionRepository;
    }

    @Override
    public void applyCustomerEvent(CustomerEventPayload payload) {
        if (payload == null || payload.customerId() == null) {
            log.warn("Müşteri olayı kimlik içermiyor, atlanıyor: {}", payload);
            return;
        }

        Long customerId = payload.customerId();

        if (CustomerEvents.CUSTOMER_DELETED.equals(payload.eventType())) {
            applyDelete(customerId);
            return;
        }

        applyUpsert(customerId, payload);
    }

    /** Müşteriyi oluşturur/günceller ve adres kümesini tam olarak yeniden yazar. */
    private void applyUpsert(Long customerId, CustomerEventPayload payload) {
        CustomerProjection customer = customerProjectionRepository.findById(customerId)
                .orElseGet(() -> {
                    CustomerProjection fresh = new CustomerProjection();
                    fresh.setCustomerId(customerId);
                    return fresh;
                });

        // Ad/soyad yalnızca olayda geldiyse güncellenir (null -> mevcut değeri koru).
        if (payload.firstName() != null) {
            customer.setFirstName(payload.firstName());
        }
        if (payload.lastName() != null) {
            customer.setLastName(payload.lastName());
        }
        customer.setIsActive(Boolean.TRUE);
        customer.setUpdatedDate(LocalDateTime.now());
        customerProjectionRepository.save(customer);

        // Adres kümesini tam olarak yeniden yaz (sil + ekle).
        replaceAddresses(customerId, payload.addresses());
    }

    /** Müşteriyi pasifleştirir ve adreslerini kaldırır. */
    private void applyDelete(Long customerId) {
        customerProjectionRepository.findById(customerId).ifPresent(customer -> {
            customer.setIsActive(Boolean.FALSE);
            customer.setUpdatedDate(LocalDateTime.now());
            customerProjectionRepository.save(customer);
        });
        addressProjectionRepository.deleteByCustomerId(customerId);
    }

    private void replaceAddresses(Long customerId, List<CustomerEventPayload.AddressPayload> addresses) {
        addressProjectionRepository.deleteByCustomerId(customerId);
        if (addresses == null || addresses.isEmpty()) {
            return;
        }
        List<CustomerAddressProjection> toSave = addresses.stream()
                .filter(a -> a.addressId() != null)
                .map(a -> toEntity(customerId, a))
                .toList();
        addressProjectionRepository.saveAll(toSave);
    }

    private CustomerAddressProjection toEntity(Long customerId, CustomerEventPayload.AddressPayload a) {
        CustomerAddressProjection entity = new CustomerAddressProjection();
        entity.setAddressId(a.addressId());
        entity.setCustomerId(customerId);
        entity.setCity(a.city());
        entity.setStreet(a.street());
        entity.setHouseNumber(a.houseNumber());
        entity.setAddressDescription(a.addressDescription());
        entity.setIsPrimary(a.isPrimary());
        return entity;
    }
}
