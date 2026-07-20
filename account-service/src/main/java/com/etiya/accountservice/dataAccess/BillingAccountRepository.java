package com.etiya.accountservice.dataAccess;

import com.etiya.accountservice.entities.BillingAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Fatura hesabı (BillingAccount) veri erişimi.
 *
 * <p>Soft-delete gereği yalnızca aktif ({@code is_active = true}) kayıtlar dönen
 * türetilmiş sorgular sağlanır. Listeleme, kabul kriteri gereği (birden fazla
 * sayfaya yayılan hesaplarda sayfalama) {@link Pageable} ile yapılır.
 */
@Repository
public interface BillingAccountRepository extends JpaRepository<BillingAccount, Long> {

    /** Aktif (silinmemiş) fatura hesabını id ile getirir. */
    Optional<BillingAccount> findByIdAndDeletedDateIsNull(Long id);

    /** Tüm aktif fatura hesaplarını sayfalı getirir. */
    Page<BillingAccount> findAllByDeletedDateIsNull(Pageable pageable);

    /** Bir müşteriye bağlı tüm aktif fatura hesaplarını getirir (Customer Account ekranı). */
    List<BillingAccount> findAllByCustomerIdAndDeletedDateIsNull(Long customerId);

    /** Aktif bir fatura hesabının id ile var olup olmadığını kontrol eder. */
    boolean existsByIdAndDeletedDateIsNull(Long id);

    /** Verilen hesap numarası aktif bir kayıtta zaten kullanılıyor mu? */
    boolean existsByAccountNumberAndDeletedDateIsNull(String accountNumber);
}
