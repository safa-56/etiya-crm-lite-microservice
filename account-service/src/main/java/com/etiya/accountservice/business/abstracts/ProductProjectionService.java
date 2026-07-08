package com.etiya.accountservice.business.abstracts;

import com.etiya.accountservice.business.dtos.events.ProductEventPayload;

/**
 * product-service olaylarını yerel projeksiyona (hesabın aktif ürün sayısı)
 * uygulayan servis.
 *
 * <p>Bu projeksiyon, "aktif ürünü olan hesap silinemez" iş kuralının veri
 * kaynağıdır. product-service henüz yazılmadığından tüketici devrede olsa da
 * olay akmayana kadar tüm sayaçlar 0 kalır.
 */
public interface ProductProjectionService {

    /** Bir ürün olayını uygular (ilgili hesabın aktif ürün sayısını günceller). */
    void applyProductEvent(ProductEventPayload payload);
}
