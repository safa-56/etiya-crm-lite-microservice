package com.etiya.orderservice.dataAccess;

import com.etiya.orderservice.entities.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Sipariş (Order) veri erişimi.
 *
 * <p>Soft-delete gereği yalnızca aktif ({@code is_active = true}) kayıtlar dönen
 * türetilmiş sorgular sağlanır.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /** Aktif (silinmemiş) siparişi id ile getirir. */
    Optional<Order> findByIdAndDeletedDateIsNull(Long id);

    /** Tüm aktif siparişleri sayfalı getirir. */
    Page<Order> findAllByDeletedDateIsNull(Pageable pageable);

    /** Bir müşteriye ait tüm aktif siparişleri getirir. */
    List<Order> findAllByCustomerIdAndDeletedDateIsNull(Long customerId);

    /**
     * Verilen sepet için hâlâ süren (aktif ve iptal edilmemiş) bir sipariş var mı?
     * Aynı sepetin ikinci kez submit edilmesini engelleyen iş kuralı buna dayanır.
     */
    boolean existsByCartIdAndDeletedDateIsNull(Long cartId);

    /**
     * Sipariş numarası daha önce üretilmiş mi? Soft-delete edilmiş siparişler de
     * taranır: numara kalıcı bir iş kimliğidir, silinmiş bir siparişinki yeniden
     * verilemez. Kapsam {@code order_number} üzerindeki unique kısıtla aynıdır.
     */
    boolean existsByOrderNumber(String orderNumber);
}
