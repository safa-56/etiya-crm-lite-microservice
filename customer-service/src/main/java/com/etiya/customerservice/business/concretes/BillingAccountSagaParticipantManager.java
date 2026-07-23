package com.etiya.customerservice.business.concretes;

import com.etiya.customerservice.business.abstracts.BillingAccountSagaParticipantService;
import com.etiya.customerservice.business.abstracts.OutboxService;
import com.etiya.customerservice.business.constants.BillingAccountSagaEvents;
import com.etiya.customerservice.business.constants.Messages;
import com.etiya.customerservice.business.dtos.events.BillingAccountSagaRequestedPayload;
import com.etiya.customerservice.business.dtos.events.BillingAccountSagaValidationPayload;
import com.etiya.customerservice.dataAccess.AddressRepository;
import com.etiya.customerservice.dataAccess.IndividualCustomerRepository;
import com.etiya.customerservice.entities.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Saga'nın doğrulayıcı adımıdır.
 * Account-service'in gönderdiği doğrulama isteğini(BillingAccountManager içindeki add fonksiyonunda gönderilen istek) alır,
 * müşteri ve adreri kendi otoriter veritabanından kontrol eder. Sonucu event olarak geri yayınlar
 */
@Service
public class BillingAccountSagaParticipantManager implements BillingAccountSagaParticipantService {

    private static final Logger log = LoggerFactory.getLogger(BillingAccountSagaParticipantManager.class);

    private final IndividualCustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final OutboxService outboxService;

    public BillingAccountSagaParticipantManager(IndividualCustomerRepository customerRepository,
                                                AddressRepository addressRepository,
                                                OutboxService outboxService) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.outboxService = outboxService;
    }

    @Override
    public void handleValidationRequest(BillingAccountSagaRequestedPayload request) {
        if (request == null || request.billingAccountId() == null) {
            log.warn("Saga isteği kimlik içermiyor, atlanıyor: {}", request);
            return;
        }

        Long accountId = request.billingAccountId();
        Long customerId = request.customerId();
        Long addressId = request.addressId();

        // 1) Müşteri otoriter olarak aktif mi?
        if (customerId == null || !customerRepository.existsByIdAndDeletedDateIsNull(customerId)) {
            publishFailed(accountId, customerId, addressId, Messages.SAGA_CUSTOMER_NOT_FOUND);
            return;
        }

        // 2) Adres bu müşteriye ait ve aktif mi?
        Address address = addressRepository
                .findByIdAndCustomer_IdAndDeletedDateIsNull(addressId, customerId)
                .orElse(null);
        if (address == null) {
            publishFailed(accountId, customerId, addressId, Messages.SAGA_ADDRESS_NOT_FOUND);
            return;
        }

        // 3) Başarılı: otoriter adres snapshot'ı ile doğrulandı olayını yayınla.
        publishValidated(accountId, customerId, address);
    }

    private void publishValidated(Long accountId, Long customerId, Address address) {
        BillingAccountSagaValidationPayload payload = new BillingAccountSagaValidationPayload(
                BillingAccountSagaEvents.CUSTOMER_VALIDATED,
                accountId, 
                customerId, 
                address.getId(), 
                true, 
                null,
                address.getCity(), 
                address.getStreet(),
                address.getHouseNumber(), 
                address.getAddressDescription());
                
        publish(accountId, BillingAccountSagaEvents.CUSTOMER_VALIDATED, payload);
        log.info("Saga doğrulandı. accountId={}, customerId={}", accountId, customerId);
    }

    private void publishFailed(Long accountId, Long customerId, Long addressId, String reason) {
        BillingAccountSagaValidationPayload payload = new BillingAccountSagaValidationPayload(
                BillingAccountSagaEvents.CUSTOMER_VALIDATION_FAILED,
                accountId, 
                customerId, 
                addressId, 
                false, 
                reason,
                null, 
                null, 
                null, 
                null);

        publish(accountId, BillingAccountSagaEvents.CUSTOMER_VALIDATION_FAILED, payload);
        log.info("Saga doğrulaması başarısız. accountId={}, neden={}", accountId, reason);
    }

    /** Sonuç olayını saga kanalına (aggregate=BillingAccountSaga) outbox ile yazar. */
    private void publish(Long accountId, String eventType, BillingAccountSagaValidationPayload payload) {
        outboxService.publish(
                BillingAccountSagaEvents.AGGREGATE_TYPE,
                String.valueOf(accountId),
                eventType,
                payload);
    }
}
