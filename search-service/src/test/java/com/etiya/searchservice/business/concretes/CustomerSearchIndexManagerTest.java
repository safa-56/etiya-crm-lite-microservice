package com.etiya.searchservice.business.concretes;

import com.etiya.searchservice.business.abstracts.CustomerSearchIndexService;
import com.etiya.searchservice.business.constants.SearchEvents;
import com.etiya.searchservice.business.dtos.events.BillingAccountEventPayload;
import com.etiya.searchservice.business.dtos.events.CustomerEventPayload;
import com.etiya.searchservice.dataAccess.CustomerSearchIndexRepository;
import com.etiya.searchservice.entities.CustomerSearchIndex;
import com.etiya.searchservice.entities.enums.CustomerRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Arama indeksi projeksiyon mantığının hermetik testi: customer upsert (null-safe),
 * delete, ve account/customer olaylarının SIRA-BAĞIMSIZ işlenmesi (stub satır).
 *
 * <p>{@code @Transactional}: LAZY {@code @ElementCollection} alanlarının (account/order
 * numaraları) doğrulama sırasında da açık bir Hibernate session'ıyla okunabilmesi için;
 * her test sonunda rollback ile izolasyon da sağlanır.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomerSearchIndexManagerTest {

    @Autowired
    private CustomerSearchIndexService indexService;

    @Autowired
    private CustomerSearchIndexRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void customerCreated_upsertsRowWithAllFields() {
        indexService.applyCustomerEvent(customerEvent(
                5001L, "Ahmet", "Can", "Yilmaz", "11111111111", "5550001",
                "B2C", SearchEvents.CUSTOMER_CREATED));

        CustomerSearchIndex row = repository.findByCustomerId(5001L).orElseThrow();
        assertThat(row.getFirstName()).isEqualTo("Ahmet");
        assertThat(row.getSecondName()).isEqualTo("Can");
        assertThat(row.getLastName()).isEqualTo("Yilmaz");
        assertThat(row.getNationalityId()).isEqualTo("11111111111");
        assertThat(row.getGsmNumber()).isEqualTo("5550001");
        assertThat(row.getRole()).isEqualTo(CustomerRole.B2C);
    }

    @Test
    void addressOnlySnapshot_keepsExistingNames_nullSafeUpsert() {
        indexService.applyCustomerEvent(customerEvent(
                5002L, "Ayse", null, "Yildiz", "22222222222", "5550002",
                "B2C", SearchEvents.CUSTOMER_CREATED));

        // Adres-only snapshot: tüm tanımlayıcı alanlar null → mevcut değerler korunmalı.
        indexService.applyCustomerEvent(customerEvent(
                5002L, null, null, null, null, null, null, SearchEvents.CUSTOMER_UPDATED));

        CustomerSearchIndex row = repository.findByCustomerId(5002L).orElseThrow();
        assertThat(row.getFirstName()).isEqualTo("Ayse");
        assertThat(row.getLastName()).isEqualTo("Yildiz");
        assertThat(row.getNationalityId()).isEqualTo("22222222222");
        assertThat(row.getRole()).isEqualTo(CustomerRole.B2C);
    }

    @Test
    void customerDeleted_removesRow() {
        indexService.applyCustomerEvent(customerEvent(
                5003L, "Mehmet", null, "Kaya", "33333333333", "5550003",
                "B2C", SearchEvents.CUSTOMER_CREATED));
        assertThat(repository.findByCustomerId(5003L)).isPresent();

        indexService.applyCustomerEvent(customerEvent(
                5003L, null, null, null, null, null, null, SearchEvents.CUSTOMER_DELETED));
        assertThat(repository.findByCustomerId(5003L)).isEmpty();
    }

    @Test
    void accountEventBeforeCustomerEvent_createsStubThenGetsFilled() {
        // Account olayı önce gelir → stub satır oluşur (numaralarla, isim yok).
        indexService.applyBillingAccountEvent(accountEvent(
                6001L, "ACC-900", "ORD-9", SearchEvents.ACCOUNT_STATUS_ACTIVE));

        CustomerSearchIndex stub = repository.findByCustomerId(6001L).orElseThrow();
        assertThat(stub.getFirstName()).isNull();
        assertThat(stub.getRole()).isEqualTo(CustomerRole.B2C);
        assertThat(stub.getAccountNumbers()).containsExactly("ACC-900");
        assertThat(stub.getOrderNumbers()).containsExactly("ORD-9");

        // Sonra customer olayı gelir → isim/TCKN/GSM dolar, numaralar korunur.
        indexService.applyCustomerEvent(customerEvent(
                6001L, "Zeynep", null, "Demir", "44444444444", "5550004",
                "B2C", SearchEvents.CUSTOMER_CREATED));

        CustomerSearchIndex filled = repository.findByCustomerId(6001L).orElseThrow();
        assertThat(filled.getFirstName()).isEqualTo("Zeynep");
        assertThat(filled.getLastName()).isEqualTo("Demir");
        assertThat(filled.getAccountNumbers()).containsExactly("ACC-900");
        assertThat(filled.getOrderNumbers()).containsExactly("ORD-9");
    }

    @Test
    void accountCancelled_removesNumbersFromIndex() {
        indexService.applyCustomerEvent(customerEvent(
                6002L, "Ali", null, "Vural", "55555555555", "5550005",
                "B2C", SearchEvents.CUSTOMER_CREATED));
        indexService.applyBillingAccountEvent(accountEvent(
                6002L, "ACC-901", "ORD-10", SearchEvents.ACCOUNT_STATUS_ACTIVE));
        assertThat(repository.findByCustomerId(6002L).orElseThrow().getAccountNumbers())
                .containsExactly("ACC-901");

        // Hesap iptal edilir (saga telafisi) → numaralar çıkarılır.
        indexService.applyBillingAccountEvent(accountEvent(
                6002L, "ACC-901", "ORD-10", SearchEvents.ACCOUNT_STATUS_CANCELLED));

        CustomerSearchIndex row = repository.findByCustomerId(6002L).orElseThrow();
        assertThat(row.getAccountNumbers()).isEmpty();
        assertThat(row.getOrderNumbers()).isEmpty();
        // Müşteri satırı kendisi silinmez.
        assertThat(row.getFirstName()).isEqualTo("Ali");
    }

    // ------------------------------------------------------------------ yardımcılar

    private CustomerEventPayload customerEvent(Long customerId, String firstName, String secondName,
                                               String lastName, String nationalityId, String gsm,
                                               String role, String eventType) {
        return new CustomerEventPayload(
                customerId, firstName, secondName, lastName, nationalityId, gsm, role,
                eventType, LocalDateTime.now());
    }

    private BillingAccountEventPayload accountEvent(Long customerId, String accountNumber,
                                                    String orderNumber, String status) {
        return new BillingAccountEventPayload(
                999L, customerId, "Hesap", accountNumber, orderNumber, status, LocalDateTime.now());
    }
}
