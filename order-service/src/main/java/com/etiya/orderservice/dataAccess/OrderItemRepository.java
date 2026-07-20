package com.etiya.orderservice.dataAccess;

import com.etiya.orderservice.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Sipariş satırı (OrderItem) veri erişimi.
 *
 * <p>Yalnızca aktif (silinmemiş) satırlar üzerinden çalışan türetilmiş sorgular
 * sağlar; sipariş yanıtı ve toplam bunlara dayanır.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /** Bir siparişin tüm aktif satırlarını getirir. */
    List<OrderItem> findAllByOrderIdAndDeletedDateIsNull(Long orderId);
}
