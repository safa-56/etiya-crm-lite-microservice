package com.etiya.orderservice.business.concretes;

import com.etiya.orderservice.business.abstracts.OrderService;
import com.etiya.orderservice.business.abstracts.OutboxService;
import com.etiya.orderservice.business.abstracts.ReferenceDataService;
import com.etiya.orderservice.business.constants.Messages;
import com.etiya.orderservice.business.constants.OrderCheckoutSagaEvents;
import com.etiya.orderservice.business.constants.OrderReferenceCodes;
import com.etiya.orderservice.business.dtos.events.OrderCheckoutRequestedPayload;
import com.etiya.orderservice.business.dtos.requests.SubmitOrderRequest;
import com.etiya.orderservice.business.dtos.responses.OrderItemResponse;
import com.etiya.orderservice.business.dtos.responses.OrderResponse;
import com.etiya.orderservice.business.dtos.responses.PagedResponse;
import com.etiya.orderservice.business.mappers.OrderMapper;
import com.etiya.orderservice.business.rules.OrderBusinessRules;
import com.etiya.orderservice.core.constants.CacheNames;
import com.etiya.orderservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.orderservice.dataAccess.OrderItemRepository;
import com.etiya.orderservice.dataAccess.OrderRepository;
import com.etiya.orderservice.entities.Order;
import com.etiya.orderservice.entities.OrderItem;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Sipariş (Order) iş mantığı (business/concretes) — FR-016 "Siparişin Tamamlanması".
 *
 * <p>Kullanıcı bir sepeti "Submit Order" ile onayladığında {@link #submit} sistem
 * tarafından benzersiz bir sipariş numarası üretir, servis adresini kaydeder ve siparişi
 * {@link OrderStatus#PENDING} açar. Sepetin otoriter içeriği (satırlar, fiyat, toplam)
 * cart-service'e <b>senkron çağrı yapılmadan</b> bir <b>choreography Saga</b> ile alınır
 * (product ↔ account / cart ↔ product modeliyle aynı): doğrulama isteği aynı transaction
 * içinde outbox'a yazılır ({@link OrderCheckoutSagaEvents#CHECKOUT_REQUESTED}); sonuç
 * geldiğinde {@link OrderSagaManager} siparişi CONFIRMED (satır/toplam snapshot'ıyla) ya da
 * CANCELLED (telafi) yapar. Okuma sonuçları Redis'te cache'lenir.
 */
@Service
public class OrderManager implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper mapper;
    private final OrderBusinessRules rules;
    private final OutboxService outboxService;
    private final ReferenceDataService referenceDataService;

    public OrderManager(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        OrderMapper mapper,
                        OrderBusinessRules rules,
                        OutboxService outboxService,
                        ReferenceDataService referenceDataService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.mapper = mapper;
        this.rules = rules;
        this.outboxService = outboxService;
        this.referenceDataService = referenceDataService;
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.ORDER_LIST, allEntries = true)
    public OrderResponse submit(SubmitOrderRequest request) {
        rules.checkIfCartNotAlreadyOrdered(request.cartId());

        // Saga adım 1: siparişi PENDING aç (kalemler/toplam doğrulama sonucuyla gelecek).
        Order order = mapper.toEntity(request);
        order.setOrderNumber(generateOrderNumber());
        order.setGeneralStatus(referenceDataService.getStatus(
                OrderReferenceCodes.ENTITY_CUSTOMER_ORDER, OrderReferenceCodes.STATUS_IN_PROGRESS_CODE));
        Order saved = orderRepository.save(order);

        requestValidation(saved);
        return buildResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.ORDERS, key = "#id")
    public OrderResponse getById(Long id) {
        return buildResponse(findActiveOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.ORDER_LIST,
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public PagedResponse<OrderResponse> getAll(Pageable pageable) {
        return PagedResponse.of(orderRepository.findAllByDeletedDateIsNull(pageable).map(this::buildResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getByCustomer(Long customerId) {
        return orderRepository.findAllByCustomerIdAndDeletedDateIsNull(customerId).stream()
                .map(this::buildResponse)
                .toList();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.ORDERS, key = "#id"),
            @CacheEvict(value = CacheNames.ORDER_LIST, allEntries = true)
    })
    public void delete(Long id) {
        Order order = findActiveOrThrow(id);
        LocalDateTime now = LocalDateTime.now();

        // Siparişi ve tüm silinmemiş satırlarını soft-delete et (durum DEL).
        var itemDeletedStatus = referenceDataService.getStatus(
                OrderReferenceCodes.ENTITY_ORDER_ITEM, OrderReferenceCodes.STATUS_DELETED_CODE);
        List<OrderItem> items = orderItemRepository.findAllByOrderIdAndDeletedDateIsNull(id);
        for (OrderItem item : items) {
            item.setGeneralStatus(itemDeletedStatus);
            item.setDeletedDate(now);
        }
        orderItemRepository.saveAll(items);

        order.setGeneralStatus(referenceDataService.getStatus(
                OrderReferenceCodes.ENTITY_CUSTOMER_ORDER, OrderReferenceCodes.STATUS_DELETED_CODE));
        order.setDeletedDate(now);
        orderRepository.save(order);
    }

    // ------------------------------------------------------------------ yardımcılar

    /** Saga adım 1 olayını (aynı transaction — outbox) yayınlar: sepet doğrulama isteği. */
    private void requestValidation(Order order) {
        outboxService.publish(
                OrderCheckoutSagaEvents.AGGREGATE_TYPE,
                String.valueOf(order.getId()),
                OrderCheckoutSagaEvents.CHECKOUT_REQUESTED,
                new OrderCheckoutRequestedPayload(
                        OrderCheckoutSagaEvents.CHECKOUT_REQUESTED,
                        order.getId(),
                        order.getCartId()));
    }

    /** Sistem tarafından üretilen benzersiz sipariş numarası (Order ID) — FR-016 ACC-02. */
    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /** Siparişi, satırları ve türetilmiş toplam tutarıyla birlikte yanıta dönüştürür. */
    private OrderResponse buildResponse(Order order) {
        List<OrderItemResponse> items = orderItemRepository.findAllByOrderIdAndDeletedDateIsNull(order.getId())
                .stream()
                .sorted(Comparator.comparing(OrderItem::getId))
                .map(this::toItemResponse)
                .toList();

        // Toplam, doğrulanmış (snapshot'lanmış) satırlardan türetilir; PENDING siparişte 0.
        BigDecimal totalAmount = order.getTotalAmount() != null
                ? order.getTotalAmount()
                : items.stream().map(OrderItemResponse::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCartId(),
                order.getCustomerId(),
                order.getAccountId(),
                order.getServiceAddressId(),
                order.getServiceAddress(),
                order.getGeneralStatus().getShortCode(),
                order.getStatusReason(),
                items,
                totalAmount,
                order.getCreatedDate(),
                order.getUpdatedDate());
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getItemType(),
                item.getProductOfferId(),
                item.getCampaignId(),
                item.getName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.lineTotal());
    }

    private Order findActiveOrThrow(Long id) {
        return orderRepository.findByIdAndDeletedDateIsNull(id)
                .orElseThrow(() -> new BusinessException(Messages.ORDER_NOT_FOUND));
    }
}
