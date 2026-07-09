package com.etiya.productservice.business.rules;

import com.etiya.productservice.business.constants.Messages;
import com.etiya.productservice.core.crosscutting.exceptions.BusinessException;
import com.etiya.productservice.dataAccess.ProductOfferRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Ürün teklifi iş kuralları.
 */
@Service
public class ProductOfferBusinessRules {

    private final ProductOfferRepository productOfferRepository;

    public ProductOfferBusinessRules(ProductOfferRepository productOfferRepository) {
        this.productOfferRepository = productOfferRepository;
    }

    /** Aktif bir teklif id ile var olmalı; yoksa iş hatası fırlatılır. */
    public void checkIfProductOfferExists(Long id) {
        if (!productOfferRepository.existsByIdAndIsActiveTrue(id)) {
            throw new BusinessException(Messages.PRODUCT_OFFER_NOT_FOUND);
        }
    }

    /** Bitiş tarihi verilmişse başlangıçtan önce olamaz. */
    public void checkDateRangeValid(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException(Messages.PRODUCT_OFFER_DATE_RANGE_INVALID);
        }
    }
}
