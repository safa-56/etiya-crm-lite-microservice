package com.etiya.cartservice.business.rules;

import com.etiya.cartservice.business.constants.Messages;
import com.etiya.cartservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.cartservice.dataAccess.CartRepository;
import org.springframework.stereotype.Service;

/**
 * Sepet (Cart) iş kuralları.
 *
 * <p>İş katmanındaki veri/durum bağımlı kontrolleri toplar; ilgili business sınıfına
 * (manager) inject edilir. Kural ihlallerinde {@link BusinessException} fırlatılır
 * (mesajlar {@link Messages} sabitlerinden — magic string yok).
 */
@Service
public class CartBusinessRules {

    private final CartRepository cartRepository;

    public CartBusinessRules(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    /** Aktif bir sepet id ile var olmalı; yoksa iş hatası fırlatılır. */
    public void checkIfCartExists(Long id) {
        if (!cartRepository.existsByIdAndDeletedDateIsNull(id)) {
            throw new BusinessException(Messages.CART_NOT_FOUND);
        }
    }

    /**
     * Aynı müşteri + fatura hesabı için zaten aktif bir sepet varsa hata verir
     * (bir müşteri-hesap ikilisi için tek aktif sepet ilkesi).
     */
    public void checkIfCartNotAlreadyExists(Long customerId, Long accountId) {
        if (cartRepository.existsByCustomerIdAndAccountIdAndDeletedDateIsNull(customerId, accountId)) {
            throw new BusinessException(Messages.CART_ALREADY_EXISTS);
        }
    }
}
