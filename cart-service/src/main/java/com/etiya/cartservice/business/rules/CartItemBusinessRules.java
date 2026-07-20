package com.etiya.cartservice.business.rules;

import com.etiya.cartservice.business.constants.Messages;
import com.etiya.cartservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.cartservice.dataAccess.CartItemRepository;
import org.springframework.stereotype.Service;

/**
 * Sepet satırı (CartItem) iş kuralları.
 *
 * <p>Sepete eklemenin <b>sepet-lokal</b> kontrollerini toplar. Teklif/kampanya varlığı
 * ve fiyatı burada değil, <b>Saga</b> ile product-service tarafından otoriter olarak
 * doğrulanır (bu servis senkron çağrı yapmaz / yerel projeksiyon tutmaz). İlgili
 * business sınıfına (manager) inject edilir.
 */
@Service
public class CartItemBusinessRules {

    private final CartItemRepository cartItemRepository;

    public CartItemBusinessRules(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    /**
     * Aynı kampanya sepette (bekleyen ya da aktif) zaten varsa hata verir
     * (paket bir kez eklenir).
     */
    public void checkCampaignNotAlreadyInCart(Long cartId, Long campaignId) {
        if (cartItemRepository.existsByCartIdAndCampaignIdAndDeletedDateIsNull(cartId, campaignId)) {
            throw new BusinessException(Messages.CAMPAIGN_ALREADY_IN_CART);
        }
    }
}
