package com.etiya.productservice.business.dtos.responses;

import java.math.BigDecimal;

/**
 * Fatura hesabına bağlı ürün detayı yanıtı (FR-013).
 *
 * <p>Ürün detay tablosu ({@code Product ID, Product Name, Campaign Name,
 * Campaign ID}) ve Product Detail modal penceresi ({@code Product Offer Name,
 * Product Offer ID, Product Spec ID, Product Char, Address}) bu salt okunur
 * yanıttan beslenir.
 *
 * @param productChar teknik özellik detayı (ProductSpec açıklamasından türetilir)
 */
public record ProductDetailResponse(
        Long productId,
        String productName,
        Long productOfferId,
        String productOfferName,
        Long productSpecId,
        String productChar,
        Long campaignId,
        String campaignName,
        Long addressId,
        BigDecimal priceToBePaid
) {
}
