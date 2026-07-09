package com.etiya.cartservice.dataAccess;

import com.etiya.cartservice.entities.Cart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Sepet (Cart) veri erişimi.
 *
 * <p>Soft-delete gereği yalnızca aktif ({@code is_active = true}) kayıtlar dönen
 * türetilmiş sorgular sağlanır.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /** Aktif (silinmemiş) sepeti id ile getirir. */
    Optional<Cart> findByIdAndIsActiveTrue(Long id);

    /** Tüm aktif sepetleri sayfalı getirir. */
    Page<Cart> findAllByIsActiveTrue(Pageable pageable);

    /** Bir müşteriye ait tüm aktif sepetleri getirir. */
    List<Cart> findAllByCustomerIdAndIsActiveTrue(Long customerId);

    /** Aktif bir sepetin id ile var olup olmadığını kontrol eder. */
    boolean existsByIdAndIsActiveTrue(Long id);

    /** Verilen müşteri + fatura hesabı için zaten aktif bir sepet var mı? */
    boolean existsByCustomerIdAndAccountIdAndIsActiveTrue(Long customerId, Long accountId);
}
