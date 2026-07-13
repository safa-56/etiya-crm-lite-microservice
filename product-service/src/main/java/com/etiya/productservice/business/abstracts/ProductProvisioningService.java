package com.etiya.productservice.business.abstracts;

import com.etiya.productservice.business.dtos.events.OrderConfirmedPayload;

/**
 * Sipariş kaynaklı ürün provizyonu servisi.
 *
 * <p>order-service'ten gelen "sipariş kesinleşti" olayını alıp sipariş kalemlerinden
 * {@code Product} kayıtları üretir. Ürünler {@code PENDING} açılır ve mevcut Product Sale
 * Saga'sı ({@code crm.ProductSaga.events}) ile fatura hesabına karşı doğrulanır; onaylanınca
 * ACTIVE olur ve {@code crm.Product.events}'e {@code ProductCreated} yayınlanarak hesabın
 * aktif ürün sayacı artar.
 *
 * <p>Çağıran (Inbox) transaction'ı içinde çalıştırılmalıdır; böylece üretilen ürünler,
 * saga istekleri (outbox) ve inbox kaydı atomik olur.
 */
public interface ProductProvisioningService {

    /**
     * Kesinleşmiş bir siparişin kalemlerini ürünlere dönüştürür (provizyon).
     *
     * @param payload order-service'ten gelen sipariş kesinleşti olayı
     */
    void provisionFromOrder(OrderConfirmedPayload payload);
}
