package com.etiya.productservice.business.concretes;

import com.etiya.productservice.business.abstracts.OutboxService;
import com.etiya.productservice.business.abstracts.ProductProvisioningService;
import com.etiya.productservice.business.abstracts.ReferenceDataService;
import com.etiya.productservice.business.constants.OrderProvisioningEvents;
import com.etiya.productservice.business.constants.ProductReferenceCodes;
import com.etiya.productservice.business.constants.ProductSagaEvents;
import com.etiya.productservice.business.dtos.events.OrderConfirmedPayload;
import com.etiya.productservice.business.dtos.events.OrderProvisionLine;
import com.etiya.productservice.business.dtos.events.ProductSagaRequestedPayload;
import com.etiya.productservice.dataAccess.CampaignOfferRepository;
import com.etiya.productservice.dataAccess.CampaignRepository;
import com.etiya.productservice.dataAccess.ProductOfferRepository;
import com.etiya.productservice.dataAccess.ProductRepository;
import com.etiya.productservice.entities.Campaign;
import com.etiya.productservice.entities.Product;
import com.etiya.productservice.entities.ProductOffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * {@link ProductProvisioningService} uygulaması — sipariş kaynaklı ürün provizyonu.
 *
 * <p>order-service'in "sipariş kesinleşti" olayındaki her kalemi bir ya da birden çok
 * {@code Product}'a dönüştürür:
 * <ul>
 *   <li><b>OFFER</b>    : teklif kimliğinden tek ürün üretilir; ödenen fiyat sipariş
 *       satırının snapshot fiyatıdır.</li>
 *   <li><b>CAMPAIGN</b> : kampanyanın paket içeriği (aktif {@code CampaignOffer} bağları)
 *       product-service'in <b>otoriter</b> DB'sinden çözülür ve her teklif için bir ürün
 *       üretilir (kampanya bağı {@code campaign_id} ile korunur; sipariş yalnızca
 *       {@code campaignId} taşır, paket açılımını burası yapar).</li>
 * </ul>
 *
 * <p>Üretilen her ürün {@code PENDING} açılır ve mevcut Product Sale Saga'sı ile
 * ({@link ProductSagaEvents#SALE_REQUESTED}) fatura hesabına karşı doğrulanır — bu,
 * REST üzerinden ürün ekleme akışıyla ({@code ProductManager.add}) birebir aynı adımdır.
 * Onaylanınca ürün ACTIVE olur ve {@code crm.Product.events}'e {@code ProductCreated}
 * yayınlanarak account-service'teki aktif ürün sayacı artar.
 *
 * <p>Çağıran (Inbox) transaction'ı içinde çalışır; ürünler + saga istekleri (outbox) +
 * inbox kaydı atomik commit edilir. Idempotency, olayın {@code messageId}'si üzerinden
 * Inbox tarafından sağlanır (aynı sipariş olayı tekrar gelse ürünler bir kez üretilir).
 */
@Service
public class ProductProvisioningManager implements ProductProvisioningService {

    private static final Logger log = LoggerFactory.getLogger(ProductProvisioningManager.class);

    private final ProductRepository productRepository;
    private final ProductOfferRepository productOfferRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignOfferRepository campaignOfferRepository;
    private final OutboxService outboxService;
    private final ReferenceDataService referenceDataService;

    public ProductProvisioningManager(ProductRepository productRepository,
                                      ProductOfferRepository productOfferRepository,
                                      CampaignRepository campaignRepository,
                                      CampaignOfferRepository campaignOfferRepository,
                                      OutboxService outboxService,
                                      ReferenceDataService referenceDataService) {
        this.productRepository = productRepository;
        this.productOfferRepository = productOfferRepository;
        this.campaignRepository = campaignRepository;
        this.campaignOfferRepository = campaignOfferRepository;
        this.outboxService = outboxService;
        this.referenceDataService = referenceDataService;
    }

    @Override
    public void provisionFromOrder(OrderConfirmedPayload payload) {
        if (payload == null || payload.orderId() == null || payload.accountId() == null) {
            log.warn("Sipariş provizyon olayı eksik (orderId/accountId yok), atlanıyor: {}", payload);
            return;
        }

        List<OrderProvisionLine> lines = payload.items() == null ? List.of() : payload.items();
        if (lines.isEmpty()) {
            log.warn("Sipariş provizyonu boş kalem listesiyle geldi, atlanıyor. orderId={}", payload.orderId());
            return;
        }

        int provisioned = 0;
        for (OrderProvisionLine line : lines) {
            provisioned += provisionLine(payload, line);
        }
        log.info("Sipariş provizyonu tamamlandı. orderId={}, üretilen ürün sayısı={}",
                payload.orderId(), provisioned);
    }

    /** Bir sipariş satırını türüne göre bir/çok ürüne dönüştürür; üretilen ürün sayısını döner. */
    private int provisionLine(OrderConfirmedPayload payload, OrderProvisionLine line) {
        String itemType = line.itemType();
        if (OrderProvisioningEvents.ITEM_TYPE_OFFER.equals(itemType)) {
            return provisionOffer(payload, line);
        }
        if (OrderProvisioningEvents.ITEM_TYPE_CAMPAIGN.equals(itemType)) {
            return provisionCampaign(payload, line);
        }
        log.warn("Bilinmeyen provizyon kalem türü '{}', atlanıyor. orderId={}", itemType, payload.orderId());
        return 0;
    }

    /** OFFER kalemi: teklif kimliğinden tek ürün üretir (ödenen fiyat = sipariş snapshot'ı). */
    private int provisionOffer(OrderConfirmedPayload payload, OrderProvisionLine line) {
        ProductOffer offer = line.productOfferId() == null ? null
                : productOfferRepository.findByIdAndDeletedDateIsNull(line.productOfferId()).orElse(null);
        if (offer == null) {
            log.warn("Provizyon için teklif bulunamadı/aktif değil (productOfferId={}), kalem atlanıyor. orderId={}",
                    line.productOfferId(), payload.orderId());
            return 0;
        }

        BigDecimal price = line.unitPrice() != null ? line.unitPrice() : offer.getPrice();
        String name = line.name() != null ? line.name() : offer.getName();
        provisionProduct(payload, offer, null, name, price);
        return 1;
    }

    /**
     * CAMPAIGN kalemi: kampanyanın paket içeriğini otoriter DB'den çözer ve her teklif için
     * bir ürün üretir. Her ürünün ödenen fiyatı ilgili teklifin liste fiyatıdır (kampanya
     * tek paket fiyatı ürün başına dağıtılmaz; ürün başına somut teklif fiyatı yazılır).
     */
    private int provisionCampaign(OrderConfirmedPayload payload, OrderProvisionLine line) {
        Campaign campaign = line.campaignId() == null ? null
                : campaignRepository.findByIdAndDeletedDateIsNull(line.campaignId()).orElse(null);
        if (campaign == null) {
            log.warn("Provizyon için kampanya bulunamadı/aktif değil (campaignId={}), kalem atlanıyor. orderId={}",
                    line.campaignId(), payload.orderId());
            return 0;
        }

        List<ProductOffer> offers = resolveCampaignOffers(campaign.getId());
        if (offers.isEmpty()) {
            log.warn("Kampanyanın aktif teklifi yok (campaignId={}), kalem atlanıyor. orderId={}",
                    campaign.getId(), payload.orderId());
            return 0;
        }

        for (ProductOffer offer : offers) {
            provisionProduct(payload, offer, campaign, offer.getName(), offer.getPrice());
        }
        return offers.size();
    }

    /** Kampanyanın aktif paket içeriğini (teklif kayıtları) id'ye göre sıralı çözer. */
    private List<ProductOffer> resolveCampaignOffers(Long campaignId) {
        List<Long> offerIds = campaignOfferRepository.findAllByCampaignIdAndDeletedDateIsNull(campaignId).stream()
                .map(link -> link.getProductOffer().getId())
                .toList();
        if (offerIds.isEmpty()) {
            return List.of();
        }
        return productOfferRepository.findAllByIdInAndDeletedDateIsNull(offerIds).stream()
                .sorted(Comparator.comparing(ProductOffer::getId))
                .toList();
    }

    /**
     * Tek bir ürünü {@code PENDING} olarak açar ve Product Sale Saga adım 1 olayını
     * ({@link ProductSagaEvents#SALE_REQUESTED}) aynı transaction — outbox — ile yayınlar.
     */
    private void provisionProduct(OrderConfirmedPayload payload, ProductOffer offer,
                                  Campaign campaign, String name, BigDecimal price) {
        Product product = new Product();
        product.setProductOffer(offer);
        product.setCampaign(campaign);
        product.setAccountId(payload.accountId());
        product.setAddressId(payload.addressId());
        product.setPriceToBePaid(price != null ? price : BigDecimal.ZERO);
        product.setName(name != null ? name : offer.getName());
        product.setGeneralStatus(referenceDataService.getStatus(
                ProductReferenceCodes.ENTITY_PRODUCT, ProductReferenceCodes.STATUS_PENDING_CODE));

        Product saved = productRepository.save(product);

        outboxService.publish(
                ProductSagaEvents.AGGREGATE_TYPE,
                String.valueOf(saved.getId()),
                ProductSagaEvents.SALE_REQUESTED,
                new ProductSagaRequestedPayload(
                        ProductSagaEvents.SALE_REQUESTED, saved.getId(), saved.getAccountId()));
    }
}
