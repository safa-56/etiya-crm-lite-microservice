package com.etiya.searchservice.business.concretes;

import com.etiya.searchservice.business.abstracts.CustomerSearchIndexService;
import com.etiya.searchservice.business.constants.SearchEvents;
import com.etiya.searchservice.business.dtos.events.BillingAccountEventPayload;
import com.etiya.searchservice.business.dtos.events.CustomerEventPayload;
import com.etiya.searchservice.core.constants.CacheNames;
import com.etiya.searchservice.dataAccess.CustomerSearchIndexRepository;
import com.etiya.searchservice.entities.CustomerSearchIndex;
import com.etiya.searchservice.entities.enums.CustomerRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Müşteri arama indeksinin olaylardan beslenmesi (projeksiyon/upsert tarafı).
 *
 * <p>İki akış birbirinden bağımsız sırada gelebilir:
 * <ul>
 *   <li><b>customer olayı</b> → satırı {@code customerId}'ye göre upsert eder
 *       (yalnızca dolu alanlar yazılır; adres-only snapshot'ta isim/TCKN/GSM null
 *       gelebilir, mevcut değer korunur). Delete → satırı kaldırır.</li>
 *   <li><b>account olayı</b> → ilgili müşteri satırına account/order numarası ekler
 *       (satır yoksa <b>stub</b> oluşturur); hesap iptal/pasif olunca çıkarır.</li>
 * </ul>
 *
 * <p>İndeks değiştiğinde arama cache'i tamamen boşaltılır (read-model tutarlılığı).
 */
@Service
public class CustomerSearchIndexManager implements CustomerSearchIndexService {

    private static final Logger log = LoggerFactory.getLogger(CustomerSearchIndexManager.class);

    private final CustomerSearchIndexRepository repository;

    public CustomerSearchIndexManager(CustomerSearchIndexRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.CUSTOMER_SEARCH, allEntries = true)
    public void applyCustomerEvent(CustomerEventPayload event) {
        if (event == null || event.customerId() == null) {
            log.warn("Müşteri olayı kimlik içermiyor, atlanıyor: {}", event);
            return;
        }

        // Delete → indeks satırını kaldır (read-model).
        if (SearchEvents.CUSTOMER_DELETED.equals(event.eventType())) {
            repository.findByCustomerId(event.customerId())
                    .ifPresent(repository::delete);
            log.debug("Müşteri silindi, indeksten kaldırıldı. customerId={}", event.customerId());
            return;
        }

        // Create/Update → upsert (yalnızca dolu alanlar; null-safe).
        CustomerSearchIndex index = repository.findByCustomerId(event.customerId())
                .orElseGet(() -> newRow(event.customerId()));

        if (event.firstName() != null) {
            index.setFirstName(event.firstName());
        }
        if (event.secondName() != null) {
            index.setSecondName(event.secondName());
        }
        if (event.lastName() != null) {
            index.setLastName(event.lastName());
        }
        if (event.nationalityId() != null) {
            index.setNationalityId(event.nationalityId());
        }
        if (event.gsmNumber() != null) {
            index.setGsmNumber(event.gsmNumber());
        }
        if (event.role() != null) {
            index.setRole(parseRole(event.role()));
        }

        repository.save(index);
        log.debug("Müşteri indeksi upsert edildi. customerId={}", event.customerId());
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.CUSTOMER_SEARCH, allEntries = true)
    public void applyBillingAccountEvent(BillingAccountEventPayload event) {
        if (event == null || event.customerId() == null) {
            log.warn("Fatura hesabı olayı müşteri kimliği içermiyor, atlanıyor: {}", event);
            return;
        }

        String status = event.accountStatus();
        boolean active = SearchEvents.ACCOUNT_STATUS_ACTIVE.equals(status);
        boolean removed = SearchEvents.ACCOUNT_STATUS_CANCELLED.equals(status)
                || SearchEvents.ACCOUNT_STATUS_PASSIVE.equals(status);

        if (active) {
            // Hesap aktif → numaraları ekle (satır yoksa stub oluştur).
            CustomerSearchIndex index = repository.findByCustomerId(event.customerId())
                    .orElseGet(() -> newRow(event.customerId()));
            addIfPresent(index.getAccountNumbers(), event.accountNumber());
            addIfPresent(index.getOrderNumbers(), event.orderNumber());
            repository.save(index);
            log.debug("Hesap numaraları indekse eklendi. customerId={}, accountNumber={}",
                    event.customerId(), event.accountNumber());
        } else if (removed) {
            // Hesap iptal/pasif → yalnızca mevcut satırdan çıkar (yoksa atla).
            repository.findByCustomerId(event.customerId()).ifPresent(index -> {
                index.getAccountNumbers().remove(event.accountNumber());
                index.getOrderNumbers().remove(event.orderNumber());
                repository.save(index);
                log.debug("Hesap numaraları indeksten çıkarıldı. customerId={}, accountNumber={}",
                        event.customerId(), event.accountNumber());
            });
        } else {
            log.debug("Hesap durumu ({}) ekleme/çıkarma gerektirmiyor, atlanıyor. customerId={}",
                    status, event.customerId());
        }
    }

    // ------------------------------------------------------------------ yardımcılar

    /** Yeni indeks satırı (stub) — varsayılan role B2C, aktif. */
    private CustomerSearchIndex newRow(Long customerId) {
        CustomerSearchIndex index = new CustomerSearchIndex();
        index.setCustomerId(customerId);
        index.setRole(CustomerRole.B2C);
        index.setIsActive(true);
        return index;
    }

    private void addIfPresent(java.util.Set<String> target, String value) {
        if (value != null && !value.isBlank()) {
            target.add(value);
        }
    }

    /** Geçersiz/boş role {@code B2C}'ye düşer. */
    private CustomerRole parseRole(String role) {
        try {
            return CustomerRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CustomerRole.B2C;
        }
    }
}
