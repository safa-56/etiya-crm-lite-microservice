package com.etiya.productservice.business.concretes;

import com.etiya.productservice.business.abstracts.CartSagaParticipantService;
import com.etiya.productservice.business.abstracts.OutboxService;
import com.etiya.productservice.business.constants.CartSagaEvents;
import com.etiya.productservice.business.constants.Messages;
import com.etiya.productservice.business.dtos.events.CartSagaLine;
import com.etiya.productservice.business.dtos.events.CartSagaRequestedPayload;
import com.etiya.productservice.business.dtos.events.CartSagaValidationPayload;
import com.etiya.productservice.dataAccess.CampaignOfferRepository;
import com.etiya.productservice.dataAccess.CampaignRepository;
import com.etiya.productservice.dataAccess.ProductOfferRepository;
import com.etiya.productservice.entities.Campaign;
import com.etiya.productservice.entities.ProductOffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Sepet ekleme Saga'sının doğrulayıcı adımıdır (product-service).
 *
 * <p>cart-service'in gönderdiği doğrulama isteğini alır ve satır türüne göre kendi
 * otoriter DB'sinden doğrular:
 * <ul>
 *   <li><b>OFFER</b>    : teklif aktif mi? Doğruysa ad + liste fiyatıyla onaylar.</li>
 *   <li><b>CAMPAIGN</b> : kampanya aktif mi? Doğruysa ad + paket fiyatı + paket içeriği
 *       (teklif satırları) ile onaylar.</li>
 * </ul>
 * Sonuç ({@code CartItemValidated}/{@code CartItemValidationFailed}) saga kanalına
 * (aggregate={@code CartSaga}) outbox ile geri yazılır. Çağıran (Inbox) transaction'ı
 * içinde çalışır.
 */
@Service
public class CartSagaParticipantManager implements CartSagaParticipantService {

    private static final Logger log = LoggerFactory.getLogger(CartSagaParticipantManager.class);

    private static final String ITEM_TYPE_OFFER = "OFFER";
    private static final String ITEM_TYPE_CAMPAIGN = "CAMPAIGN";

    private final ProductOfferRepository productOfferRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignOfferRepository campaignOfferRepository;
    private final OutboxService outboxService;

    public CartSagaParticipantManager(ProductOfferRepository productOfferRepository,
                                      CampaignRepository campaignRepository,
                                      CampaignOfferRepository campaignOfferRepository,
                                      OutboxService outboxService) {
        this.productOfferRepository = productOfferRepository;
        this.campaignRepository = campaignRepository;
        this.campaignOfferRepository = campaignOfferRepository;
        this.outboxService = outboxService;
    }

    @Override
    public void handleValidationRequest(CartSagaRequestedPayload request) {
        if (request == null || request.cartItemId() == null || request.itemType() == null) {
            log.warn("Sepet saga isteği eksik, atlanıyor: {}", request);
            return;
        }

        switch (request.itemType()) {
            case ITEM_TYPE_OFFER -> validateOffer(request);
            case ITEM_TYPE_CAMPAIGN -> validateCampaign(request);
            default -> {
                log.warn("Bilinmeyen sepet satırı türü: {}", request.itemType());
                publishFailed(request.cartItemId(), Messages.SAGA_CART_ITEM_TYPE_UNKNOWN);
            }
        }
    }

    /** OFFER doğrulaması: teklif aktifse ad + liste fiyatıyla onaylar. */
    private void validateOffer(CartSagaRequestedPayload request) {
        ProductOffer offer = request.productOfferId() == null ? null
                : productOfferRepository.findByIdAndIsActiveTrue(request.productOfferId()).orElse(null);
        if (offer == null) {
            publishFailed(request.cartItemId(), Messages.SAGA_CART_PRODUCT_OFFER_NOT_FOUND);
            return;
        }
        publishValidated(request.cartItemId(), offer.getName(), offer.getPrice(), List.of());
    }

    /** CAMPAIGN doğrulaması: kampanya aktifse ad + paket fiyatı + içerik ile onaylar. */
    private void validateCampaign(CartSagaRequestedPayload request) {
        Campaign campaign = request.campaignId() == null ? null
                : campaignRepository.findByIdAndIsActiveTrue(request.campaignId()).orElse(null);
        if (campaign == null) {
            publishFailed(request.cartItemId(), Messages.SAGA_CART_CAMPAIGN_NOT_FOUND);
            return;
        }
        publishValidated(request.cartItemId(), campaign.getName(), campaign.getCampaignPrice(),
                resolveCampaignLines(campaign.getId()));
    }

    /** Kampanyanın aktif paket içeriğini (teklif satırları) çözer. */
    private List<CartSagaLine> resolveCampaignLines(Long campaignId) {
        List<Long> offerIds = campaignOfferRepository.findAllByCampaignIdAndIsActiveTrue(campaignId).stream()
                .map(link -> link.getProductOffer().getId())
                .toList();
        if (offerIds.isEmpty()) {
            return List.of();
        }
        return productOfferRepository.findAllByIdInAndIsActiveTrue(offerIds).stream()
                .sorted(Comparator.comparing(ProductOffer::getId))
                .map(o -> new CartSagaLine(o.getId(), o.getName(), o.getPrice()))
                .toList();
    }

    private void publishValidated(Long cartItemId, String name, BigDecimal unitPrice, List<CartSagaLine> offers) {
        CartSagaValidationPayload payload = new CartSagaValidationPayload(
                CartSagaEvents.ITEM_VALIDATED, cartItemId, true, null, name, unitPrice, offers);
        publish(cartItemId, CartSagaEvents.ITEM_VALIDATED, payload);
        log.info("Sepet saga doğrulandı. cartItemId={}", cartItemId);
    }

    private void publishFailed(Long cartItemId, String reason) {
        CartSagaValidationPayload payload = new CartSagaValidationPayload(
                CartSagaEvents.ITEM_VALIDATION_FAILED, cartItemId, false, reason, null, null, List.of());
        publish(cartItemId, CartSagaEvents.ITEM_VALIDATION_FAILED, payload);
        log.info("Sepet saga doğrulaması başarısız. cartItemId={}, neden={}", cartItemId, reason);
    }

    /** Sonuç olayını saga kanalına (aggregate=CartSaga) outbox ile yazar. */
    private void publish(Long cartItemId, String eventType, CartSagaValidationPayload payload) {
        outboxService.publish(
                CartSagaEvents.AGGREGATE_TYPE,
                String.valueOf(cartItemId),
                eventType,
                payload);
    }
}
