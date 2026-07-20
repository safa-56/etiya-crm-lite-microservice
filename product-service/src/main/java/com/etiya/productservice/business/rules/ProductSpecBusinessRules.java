package com.etiya.productservice.business.rules;

import com.etiya.productservice.business.constants.Messages;
import com.etiya.productservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.productservice.dataAccess.ProductSpecRepository;
import org.springframework.stereotype.Service;

/**
 * Ürün teknik özelliği iş kuralları.
 *
 * <p>İş katmanındaki veri/durum bağımlı kontrolleri toplar; ilgili business
 * sınıfına (manager) inject edilir. Kural ihlallerinde {@link BusinessException}
 * fırlatılır (mesajlar {@link Messages} sabitlerinden).
 */
@Service
public class ProductSpecBusinessRules {

    private final ProductSpecRepository productSpecRepository;

    public ProductSpecBusinessRules(ProductSpecRepository productSpecRepository) {
        this.productSpecRepository = productSpecRepository;
    }

    /** Aktif bir teknik özellik id ile var olmalı; yoksa iş hatası fırlatılır. */
    public void checkIfProductSpecExists(Long id) {
        if (!productSpecRepository.existsByIdAndDeletedDateIsNull(id)) {
            throw new BusinessException(Messages.PRODUCT_SPEC_NOT_FOUND);
        }
    }
}
