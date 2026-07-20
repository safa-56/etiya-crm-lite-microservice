package com.etiya.orderservice.business.rules;

import com.etiya.orderservice.business.constants.Messages;
import com.etiya.orderservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.orderservice.dataAccess.OrderRepository;
import org.springframework.stereotype.Service;

/**
 * Sipariş (Order) iş kuralları.
 *
 * <p>İş katmanındaki veri/durum bağımlı kontrolleri toplar; ilgili business sınıfına
 * (manager) inject edilir. Kural ihlallerinde {@link BusinessException} fırlatılır
 * (mesajlar {@link Messages} sabitlerinden — magic string yok).
 */
@Service
public class OrderBusinessRules {

    private final OrderRepository orderRepository;

    public OrderBusinessRules(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Aynı sepet için hâlâ süren (aktif) bir sipariş varsa hata verir. Aynı sepetin
     * yanlışlıkla iki kez submit edilmesini (mükerrer sipariş) engeller; iptal edilen
     * (soft-delete) siparişler bu kontrolün dışındadır (yeniden denenebilir).
     */
    public void checkIfCartNotAlreadyOrdered(Long cartId) {
        if (orderRepository.existsByCartIdAndDeletedDateIsNull(cartId)) {
            throw new BusinessException(Messages.ORDER_ALREADY_EXISTS_FOR_CART);
        }
    }
}
