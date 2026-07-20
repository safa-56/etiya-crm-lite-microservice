package com.etiya.cartservice.business.concretes;

import com.etiya.cartservice.business.abstracts.CartSagaService;
import com.etiya.cartservice.business.abstracts.ReferenceDataService;
import com.etiya.cartservice.business.constants.CartReferenceCodes;
import com.etiya.cartservice.business.dtos.events.CartItemSagaLine;
import com.etiya.cartservice.business.dtos.events.CartItemSagaValidationPayload;
import com.etiya.cartservice.core.constants.CacheNames;
import com.etiya.cartservice.dataAccess.CartItemRepository;
import com.etiya.cartservice.entities.CartItem;
import com.etiya.cartservice.entities.CartItemLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Sepete ekleme Saga'sının cart-service (başlatıcı) doğrulama sonucu adımıdır.
 *
 * <p>product-service'ten gelen sonucu sepet satırının durumuna göre yönlendirir
 * (idempotent): yalnızca PENDING satırlar işlenir. Onayda satır ACTIVE olur; ad/fiyat
 * (kampanyada paket içeriği) snapshot'lanır. Telafide satır CANCELLED olur ve
 * pasifleştirilir. Çağıran (Inbox) transaction'ı içinde çalışır; sepet cache'i
 * boşaltılır (içerik değişti).
 */
@Service
public class CartSagaManager implements CartSagaService {

    private static final Logger log = LoggerFactory.getLogger(CartSagaManager.class);

    private final CartItemRepository cartItemRepository;
    private final ReferenceDataService referenceDataService;

    public CartSagaManager(CartItemRepository cartItemRepository,
                           ReferenceDataService referenceDataService) {
        this.cartItemRepository = cartItemRepository;
        this.referenceDataService = referenceDataService;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.CARTS, allEntries = true),
            @CacheEvict(value = CacheNames.CART_LIST, allEntries = true)
    })
    public void applyValidationResult(CartItemSagaValidationPayload payload) {
        if (payload == null || payload.cartItemId() == null) {
            log.warn("Saga doğrulama sonucu kimlik içermiyor, atlanıyor: {}", payload);
            return;
        }

        CartItem item = cartItemRepository.findById(payload.cartItemId()).orElse(null);
        if (item == null) {
            log.warn("Saga sonucundaki sepet satırı bulunamadı (id={}), atlanıyor.", payload.cartItemId());
            return;
        }

        // Idempotency: yalnızca PNDG (Beklemede) satırlar ileri götürülür/telafi edilir.
        if (!CartReferenceCodes.STATUS_PENDING_CODE.equals(item.getGeneralStatus().getShortCode())) {
            log.debug("Sepet satırının bekleyen saga'sı yok (durum={}), sonuç atlanıyor. id={}",
                    item.getGeneralStatus().getShortCode(), item.getId());
            return;
        }

        if (payload.valid()) {
            confirm(item, payload);
        } else {
            cancel(item, payload.reason());
        }
    }

    /** Onay: satırı ACTIVE yapar; ad/fiyat ve (varsa) paket içeriği snapshot'ını yazar. */
    private void confirm(CartItem item, CartItemSagaValidationPayload payload) {
        item.setGeneralStatus(referenceDataService.getStatus(
                CartReferenceCodes.ENTITY_CART_ITEM, CartReferenceCodes.STATUS_ACTIVE_CODE));
        item.setStatusReason(null);
        item.setName(payload.name());
        item.setUnitPrice(payload.unitPrice());

        // Kampanya paket içeriği snapshot'ı (varsa) satıra yazılır.
        item.getLines().clear();
        var lineActiveStatus = referenceDataService.getStatus(
                CartReferenceCodes.ENTITY_CART_ITEM_LINE, CartReferenceCodes.STATUS_ACTIVE_CODE);
        List<CartItemSagaLine> offers = payload.offers() == null ? List.of() : payload.offers();
        for (CartItemSagaLine offer : offers) {
            CartItemLine line = new CartItemLine();
            line.setOfferId(offer.offerId());
            line.setOfferName(offer.offerName());
            line.setListPrice(offer.listPrice());
            line.setGeneralStatus(lineActiveStatus);
            item.addLine(line);
        }

        cartItemRepository.save(item);
        log.info("Sepet saga onaylandı: satır ACTIVE. id={}", item.getId());
    }

    /** Telafi (compensation): satırı CNCL (iptal) yapar ve soft-delete eder. */
    private void cancel(CartItem item, String reason) {
        item.setGeneralStatus(referenceDataService.getStatus(
                CartReferenceCodes.ENTITY_CART_ITEM, CartReferenceCodes.STATUS_CANCELLED_CODE));
        item.setStatusReason(reason);
        item.setDeletedDate(LocalDateTime.now());
        cartItemRepository.save(item);
        log.info("Sepet saga telafi edildi: satır iptal (CNCL) (neden={}). id={}", reason, item.getId());
    }
}
