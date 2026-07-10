package com.etiya.cartservice.business.concretes;

import com.etiya.cartservice.business.abstracts.OrderCheckoutParticipantService;
import com.etiya.cartservice.business.abstracts.OutboxService;
import com.etiya.cartservice.business.constants.Messages;
import com.etiya.cartservice.business.constants.OrderCheckoutSagaEvents;
import com.etiya.cartservice.business.dtos.events.OrderCheckoutRequestedPayload;
import com.etiya.cartservice.business.dtos.events.OrderCheckoutValidationPayload;
import com.etiya.cartservice.business.dtos.events.OrderSagaItemLine;
import com.etiya.cartservice.dataAccess.CartItemRepository;
import com.etiya.cartservice.dataAccess.CartRepository;
import com.etiya.cartservice.entities.Cart;
import com.etiya.cartservice.entities.CartItem;
import com.etiya.cartservice.entities.enums.CartItemStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Sepetten siparişe geçiş Saga'sının doğrulayıcı adımıdır (cart-service).
 *
 * <p>order-service'in gönderdiği checkout isteğini alır, sepeti kendi otoriter
 * veritabanından kontrol eder (var/aktif ve en az bir onaylanmış satırı var mı) ve sonucu
 * ({@code OrderCartValidated}/{@code OrderCartValidationFailed}) saga kanalına outbox ile
 * geri yayınlar. Onay sonucunda sepetin sahipliği, onaylanmış satırların snapshot'ı ve
 * toplam tutar taşınır (order-service bunları sipariş satırlarına yazar). Çağıran (Inbox)
 * transaction'ı içinde çalışır.
 */
@Service
public class OrderCheckoutParticipantManager implements OrderCheckoutParticipantService {

    private static final Logger log = LoggerFactory.getLogger(OrderCheckoutParticipantManager.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OutboxService outboxService;

    public OrderCheckoutParticipantManager(CartRepository cartRepository,
                                           CartItemRepository cartItemRepository,
                                           OutboxService outboxService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.outboxService = outboxService;
    }

    @Override
    public void handleValidationRequest(OrderCheckoutRequestedPayload request) {
        if (request == null || request.orderId() == null) {
            log.warn("Sipariş saga isteği kimlik içermiyor, atlanıyor: {}", request);
            return;
        }

        Long orderId = request.orderId();
        Long cartId = request.cartId();

        // 1) Sepet otoriter olarak var/aktif mi?
        Cart cart = cartId == null ? null
                : cartRepository.findByIdAndIsActiveTrue(cartId).orElse(null);
        if (cart == null) {
            publishFailed(orderId, cartId, Messages.SAGA_CART_NOT_FOUND);
            return;
        }

        // 2) Sepette onaylanmış (ACTIVE) en az bir satır var mı? (PENDING/CANCELLED sayılmaz)
        List<CartItem> activeItems = cartItemRepository.findAllByCartIdAndIsActiveTrue(cartId).stream()
                .filter(item -> item.getStatus() == CartItemStatus.ACTIVE)
                .sorted(Comparator.comparing(CartItem::getId))
                .toList();
        if (activeItems.isEmpty()) {
            publishFailed(orderId, cartId, Messages.SAGA_CART_EMPTY);
            return;
        }

        // 3) Başarılı: sepet sahipliği + satır snapshot'ı + toplam ile doğrulandı olayını yayınla.
        publishValidated(orderId, cart, activeItems);
    }

    private void publishValidated(Long orderId, Cart cart, List<CartItem> activeItems) {
        List<OrderSagaItemLine> lines = activeItems.stream()
                .map(item -> new OrderSagaItemLine(
                        item.getItemType().name(),
                        item.getProductOfferId(),
                        item.getCampaignId(),
                        item.getName(),
                        item.getUnitPrice(),
                        item.getQuantity()))
                .toList();

        BigDecimal totalAmount = activeItems.stream()
                .map(CartItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        OrderCheckoutValidationPayload payload = new OrderCheckoutValidationPayload(
                OrderCheckoutSagaEvents.CART_VALIDATED, orderId, cart.getId(), true, null,
                cart.getCustomerId(), cart.getAccountId(), totalAmount, lines);
        publish(orderId, OrderCheckoutSagaEvents.CART_VALIDATED, payload);
        log.info("Sipariş saga doğrulandı. orderId={}, cartId={}, toplam={}",
                orderId, cart.getId(), totalAmount);
    }

    private void publishFailed(Long orderId, Long cartId, String reason) {
        OrderCheckoutValidationPayload payload = new OrderCheckoutValidationPayload(
                OrderCheckoutSagaEvents.CART_VALIDATION_FAILED, orderId, cartId, false, reason,
                null, null, null, null);
        publish(orderId, OrderCheckoutSagaEvents.CART_VALIDATION_FAILED, payload);
        log.info("Sipariş saga doğrulaması başarısız. orderId={}, neden={}", orderId, reason);
    }

    /** Sonuç olayını saga kanalına (aggregate=OrderCheckoutSaga) outbox ile yazar. */
    private void publish(Long orderId, String eventType, OrderCheckoutValidationPayload payload) {
        outboxService.publish(
                OrderCheckoutSagaEvents.AGGREGATE_TYPE,
                String.valueOf(orderId),
                eventType,
                payload);
    }
}
