package com.etiya.cartservice.business.concretes;

import com.etiya.cartservice.business.abstracts.CartService;
import com.etiya.cartservice.business.abstracts.OutboxService;
import com.etiya.cartservice.business.constants.CartSagaEvents;
import com.etiya.cartservice.business.constants.Messages;
import com.etiya.cartservice.business.dtos.events.CartItemSagaRequestedPayload;
import com.etiya.cartservice.business.dtos.requests.AddCampaignToCartRequest;
import com.etiya.cartservice.business.dtos.requests.AddOfferToCartRequest;
import com.etiya.cartservice.business.dtos.requests.CreateCartRequest;
import com.etiya.cartservice.business.dtos.responses.CampaignOfferLineResponse;
import com.etiya.cartservice.business.dtos.responses.CartItemResponse;
import com.etiya.cartservice.business.dtos.responses.CartResponse;
import com.etiya.cartservice.business.dtos.responses.PagedResponse;
import com.etiya.cartservice.business.mappers.CartMapper;
import com.etiya.cartservice.business.rules.CartBusinessRules;
import com.etiya.cartservice.business.rules.CartItemBusinessRules;
import com.etiya.cartservice.core.constants.CacheNames;
import com.etiya.cartservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.cartservice.dataAccess.CartItemRepository;
import com.etiya.cartservice.dataAccess.CartRepository;
import com.etiya.cartservice.entities.Cart;
import com.etiya.cartservice.entities.CartItem;
import com.etiya.cartservice.entities.CartItemLine;
import com.etiya.cartservice.entities.enums.CartItemStatus;
import com.etiya.cartservice.entities.enums.CartItemType;
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

/**
 * Sepet (Cart) iş mantığı (business/concretes).
 *
 * <p>Sepet CRUD'una ek olarak FR-014'ün iki ekleme yolunu yönetir:
 * <ol>
 *   <li><b>addOffer</b>   : katalogdan doğrudan bir ürün teklifi.</li>
 *   <li><b>addCampaign</b>: içinde birden çok teklif bulunan kampanya, tek paket
 *       fiyatıyla bir bütün olarak.</li>
 * </ol>
 *
 * <p>İki yol da bir <b>choreography Saga</b> ile kesinleşir (product ↔ account
 * modeliyle aynı): satır {@link CartItemStatus#PENDING} açılır ve product-service'ten
 * teklif/kampanya doğrulaması istenir ({@link CartSagaEvents#ITEM_VALIDATION_REQUESTED},
 * aynı transaction — outbox). Doğrulama otoriter olarak product-service'e bırakılır;
 * sonuç geldiğinde {@link CartSagaManager} satırı ACTIVE (ad/fiyat/paket içeriği
 * snapshot'ıyla) ya da CANCELLED (telafi) yapar. Bu servis product-service'e senkron
 * çağrı yapmaz / yerel projeksiyon tutmaz. Okuma sonuçları Redis'te cache'lenir.
 */
@Service
public class CartManager implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartMapper mapper;
    private final CartBusinessRules cartRules;
    private final CartItemBusinessRules itemRules;
    private final OutboxService outboxService;

    public CartManager(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       CartMapper mapper,
                       CartBusinessRules cartRules,
                       CartItemBusinessRules itemRules,
                       OutboxService outboxService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.mapper = mapper;
        this.cartRules = cartRules;
        this.itemRules = itemRules;
        this.outboxService = outboxService;
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheNames.CART_LIST, allEntries = true)
    public CartResponse create(CreateCartRequest request) {
        cartRules.checkIfCartNotAlreadyExists(request.customerId(), request.accountId());

        Cart cart = mapper.toEntity(request);
        cart.setIsActive(true);
        return buildResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CARTS, key = "#id")
    public CartResponse getById(Long id) {
        return buildResponse(findActiveOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CART_LIST,
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public PagedResponse<CartResponse> getAll(Pageable pageable) {
        return PagedResponse.of(cartRepository.findAllByIsActiveTrue(pageable).map(this::buildResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartResponse> getByCustomer(Long customerId) {
        return cartRepository.findAllByCustomerIdAndIsActiveTrue(customerId).stream()
                .map(this::buildResponse)
                .toList();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.CARTS, key = "#cartId"),
            @CacheEvict(value = CacheNames.CART_LIST, allEntries = true)
    })
    public CartResponse addOffer(Long cartId, AddOfferToCartRequest request) {
        Cart cart = findActiveOrThrow(cartId);

        // Saga adım 1: satırı PENDING aç (ad/fiyat doğrulama sonucuyla gelecek).
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setItemType(CartItemType.OFFER);
        item.setProductOfferId(request.productOfferId());
        item.setQuantity(request.quantityOrDefault());
        item.setStatus(CartItemStatus.PENDING);
        item.setIsActive(true);
        CartItem saved = cartItemRepository.save(item);

        requestValidation(saved);
        return buildResponse(cart);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.CARTS, key = "#cartId"),
            @CacheEvict(value = CacheNames.CART_LIST, allEntries = true)
    })
    public CartResponse addCampaign(Long cartId, AddCampaignToCartRequest request) {
        Cart cart = findActiveOrThrow(cartId);
        itemRules.checkCampaignNotAlreadyInCart(cartId, request.campaignId());

        // Saga adım 1: kampanya satırını PENDING aç (paket fiyatı/içeriği sonuçla gelecek).
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setItemType(CartItemType.CAMPAIGN);
        item.setCampaignId(request.campaignId());
        item.setQuantity(1);
        item.setStatus(CartItemStatus.PENDING);
        item.setIsActive(true);
        CartItem saved = cartItemRepository.save(item);

        requestValidation(saved);
        return buildResponse(cart);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.CARTS, key = "#cartId"),
            @CacheEvict(value = CacheNames.CART_LIST, allEntries = true)
    })
    public CartResponse removeItem(Long cartId, Long itemId) {
        Cart cart = findActiveOrThrow(cartId);
        CartItem item = cartItemRepository.findByIdAndCartIdAndIsActiveTrue(itemId, cartId)
                .orElseThrow(() -> new BusinessException(Messages.CART_ITEM_NOT_FOUND));

        item.setIsActive(false);
        item.setDeletedDate(LocalDateTime.now());
        cartItemRepository.save(item);

        return buildResponse(cart);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.CARTS, key = "#id"),
            @CacheEvict(value = CacheNames.CART_LIST, allEntries = true)
    })
    public void delete(Long id) {
        Cart cart = findActiveOrThrow(id);
        LocalDateTime now = LocalDateTime.now();

        // Sepeti ve tüm aktif satırlarını pasifleştir (soft-delete).
        List<CartItem> items = cartItemRepository.findAllByCartIdAndIsActiveTrue(id);
        for (CartItem item : items) {
            item.setIsActive(false);
            item.setDeletedDate(now);
        }
        cartItemRepository.saveAll(items);

        cart.setIsActive(false);
        cart.setDeletedDate(now);
        cartRepository.save(cart);
    }

    // ------------------------------------------------------------------ yardımcılar

    /** Saga adım 1 olayını (aynı transaction — outbox) yayınlar: doğrulama isteği. */
    private void requestValidation(CartItem item) {
        outboxService.publish(
                CartSagaEvents.AGGREGATE_TYPE,
                String.valueOf(item.getId()),
                CartSagaEvents.ITEM_VALIDATION_REQUESTED,
                new CartItemSagaRequestedPayload(
                        CartSagaEvents.ITEM_VALIDATION_REQUESTED,
                        item.getId(),
                        item.getItemType().name(),
                        item.getProductOfferId(),
                        item.getCampaignId()));
    }

    /** Sepeti, satırları ve türetilmiş toplam tutarıyla birlikte yanıta dönüştürür. */
    private CartResponse buildResponse(Cart cart) {
        List<CartItemResponse> items = cartItemRepository.findAllByCartIdAndIsActiveTrue(cart.getId())
                .stream()
                .sorted(Comparator.comparing(CartItem::getId))
                .map(this::toItemResponse)
                .toList();

        // Toplam yalnızca fiyatı kesinleşmiş (ACTIVE) satırları içerir; PENDING satır 0 katkı verir.
        BigDecimal totalPrice = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                cart.getCustomerId(),
                cart.getAccountId(),
                items,
                totalPrice,
                cart.getIsActive(),
                cart.getCreatedDate(),
                cart.getUpdatedDate());
    }

    /** Sepet satırını yanıta dönüştürür; kampanya satırında paket içeriği snapshot'ını ekler. */
    private CartItemResponse toItemResponse(CartItem item) {
        List<CampaignOfferLineResponse> campaignOffers = item.getLines().stream()
                .sorted(Comparator.comparing(CartItemLine::getOfferId))
                .map(l -> new CampaignOfferLineResponse(l.getOfferId(), l.getOfferName(), l.getListPrice()))
                .toList();

        return new CartItemResponse(
                item.getId(),
                item.getItemType(),
                item.getStatus(),
                item.getStatusReason(),
                item.getProductOfferId(),
                item.getCampaignId(),
                item.getName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.lineTotal(),
                campaignOffers);
    }

    private Cart findActiveOrThrow(Long id) {
        return cartRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException(Messages.CART_NOT_FOUND));
    }
}
