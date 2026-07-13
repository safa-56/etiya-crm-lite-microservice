package com.etiya.orderservice.business.concretes;

import com.etiya.orderservice.business.abstracts.OrderSagaService;
import com.etiya.orderservice.business.abstracts.OutboxService;
import com.etiya.orderservice.business.constants.OrderEvents;
import com.etiya.orderservice.business.dtos.events.OrderCheckoutValidationPayload;
import com.etiya.orderservice.business.dtos.events.OrderConfirmedPayload;
import com.etiya.orderservice.business.dtos.events.OrderProvisionLine;
import com.etiya.orderservice.business.dtos.events.OrderSagaItemLine;
import com.etiya.orderservice.core.constants.CacheNames;
import com.etiya.orderservice.dataAccess.OrderRepository;
import com.etiya.orderservice.entities.Order;
import com.etiya.orderservice.entities.OrderItem;
import com.etiya.orderservice.entities.enums.OrderItemType;
import com.etiya.orderservice.entities.enums.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Sepetten siparişe geçiş Saga'sının order-service (başlatıcı) doğrulama sonucu adımıdır.
 *
 * <p>cart-service'ten gelen sonucu siparişin durumuna göre yönlendirir (idempotent):
 * yalnızca PENDING siparişler işlenir. Onayda sipariş CONFIRMED olur; sepet sahipliği
 * ({@code customerId}/{@code accountId}), toplam tutar ve kalemler sipariş satırlarına
 * snapshot'lanır. Telafide sipariş CANCELLED olur ve pasifleştirilir. Çağıran (Inbox)
 * transaction'ı içinde çalışır; sipariş cache'i boşaltılır (içerik/durum değişti).
 */
@Service
public class OrderSagaManager implements OrderSagaService {

    private static final Logger log = LoggerFactory.getLogger(OrderSagaManager.class);

    private final OrderRepository orderRepository;
    private final OutboxService outboxService;

    public OrderSagaManager(OrderRepository orderRepository, OutboxService outboxService) {
        this.orderRepository = orderRepository;
        this.outboxService = outboxService;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.ORDERS, allEntries = true),
            @CacheEvict(value = CacheNames.ORDER_LIST, allEntries = true)
    })
    public void applyValidationResult(OrderCheckoutValidationPayload payload) {
        if (payload == null || payload.orderId() == null) {
            log.warn("Saga doğrulama sonucu kimlik içermiyor, atlanıyor: {}", payload);
            return;
        }

        Order order = orderRepository.findById(payload.orderId()).orElse(null);
        if (order == null) {
            log.warn("Saga sonucundaki sipariş bulunamadı (id={}), atlanıyor.", payload.orderId());
            return;
        }

        // Idempotency: yalnızca PENDING siparişler ileri götürülür/telafi edilir.
        if (order.getStatus() != OrderStatus.PENDING) {
            log.debug("Siparişin bekleyen saga'sı yok (durum={}), sonuç atlanıyor. id={}",
                    order.getStatus(), order.getId());
            return;
        }

        if (payload.valid()) {
            confirm(order, payload);
        } else {
            cancel(order, payload.reason());
        }
    }

    /** Onay: siparişi CONFIRMED yapar; sahiplik + toplam + kalem snapshot'ını yazar. */
    private void confirm(Order order, OrderCheckoutValidationPayload payload) {
        order.setStatus(OrderStatus.CONFIRMED);
        order.setStatusReason(null);
        order.setCustomerId(payload.customerId());
        order.setAccountId(payload.accountId());
        order.setTotalAmount(payload.totalAmount());

        // Sepet kalemlerini sipariş satırlarına snapshot'la (yeniden kur).
        order.getItems().clear();
        List<OrderSagaItemLine> lines = payload.items() == null ? List.of() : payload.items();
        for (OrderSagaItemLine line : lines) {
            OrderItem item = new OrderItem();
            item.setItemType(parseType(line.itemType()));
            item.setProductOfferId(line.productOfferId());
            item.setCampaignId(line.campaignId());
            item.setName(line.name());
            item.setUnitPrice(line.unitPrice());
            item.setQuantity(line.quantity() == null ? 1 : line.quantity());
            item.setIsActive(true);
            order.addItem(item);
        }

        orderRepository.save(order);
        log.info("Sipariş saga onaylandı: CONFIRMED. id={}, toplam={}", order.getId(), payload.totalAmount());

        requestProvisioning(order);
    }

    /**
     * Ürün provizyon adımı (choreography): kesinleşmiş siparişin kalemlerini
     * {@code crm.Order.events} kanalına yayınlar. Aynı transaction (Inbox) içinde
     * outbox'a yazıldığından, durum güncellemesiyle atomik olur (ghost event yok).
     * product-service bu olayı tüketip her kalem için {@code Product} üretir.
     */
    private void requestProvisioning(Order order) {
        List<OrderProvisionLine> lines = order.getItems().stream()
                .filter(OrderItem::getIsActive)
                .map(item -> new OrderProvisionLine(
                        item.getItemType() != null ? item.getItemType().name() : null,
                        item.getProductOfferId(),
                        item.getCampaignId(),
                        item.getName(),
                        item.getUnitPrice(),
                        item.getQuantity()))
                .toList();

        if (lines.isEmpty()) {
            log.warn("Sipariş provizyone edilecek kalem içermiyor, olay yayınlanmıyor. id={}", order.getId());
            return;
        }

        outboxService.publish(
                OrderEvents.AGGREGATE_TYPE,
                String.valueOf(order.getId()),
                OrderEvents.ORDER_CONFIRMED,
                new OrderConfirmedPayload(
                        OrderEvents.ORDER_CONFIRMED,
                        order.getId(),
                        order.getAccountId(),
                        order.getServiceAddressId(),
                        lines));
    }

    /** Telafi (compensation): siparişi CANCELLED yapar ve pasifleştirir. */
    private void cancel(Order order, String reason) {
        order.setStatus(OrderStatus.CANCELLED);
        order.setStatusReason(reason);
        order.setIsActive(false);
        order.setDeletedDate(LocalDateTime.now());
        orderRepository.save(order);
        log.info("Sipariş saga telafi edildi: CANCELLED (neden={}). id={}", reason, order.getId());
    }

    /** Sepet kalem türünü ("OFFER"/"CAMPAIGN") sipariş satırı türüne çevirir (varsayılan OFFER). */
    private OrderItemType parseType(String itemType) {
        try {
            return OrderItemType.valueOf(itemType);
        } catch (IllegalArgumentException | NullPointerException e) {
            log.warn("Bilinmeyen kalem türü '{}', OFFER kabul ediliyor.", itemType);
            return OrderItemType.OFFER;
        }
    }
}
