package com.etiya.accountservice.business.abstracts;

import com.etiya.accountservice.business.dtos.events.ProductSagaRequestedPayload;

/**
 * Ürün satışı Saga'sının account-service (doğrulayıcı) adımı.
 *
 * <p>product-service'ten gelen doğrulama isteğini alır, fatura hesabını kendi
 * otoriter veritabanından kontrol eder (var mı / ACTIVE mi) ve sonucu olay olarak
 * geri yayınlar.
 */
public interface ProductSagaParticipantService {

    void handleValidationRequest(ProductSagaRequestedPayload request);
}
