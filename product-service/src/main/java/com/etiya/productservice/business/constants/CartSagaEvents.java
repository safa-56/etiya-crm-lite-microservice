package com.etiya.productservice.business.constants;

/**
 * Sepete ekleme <b>Saga</b>'sının (choreography) olay/kanal sabitleri — product-service
 * (doğrulayıcı taraf).
 *
 * <p>Aggregate tipi {@code CartSaga} olan tüm outbox kayıtları Debezium EventRouter ile
 * {@code crm.CartSaga.events} topic'ine yönlendirilir. product-service bu topic'teki
 * doğrulama İSTEKLERİNİ işler ve teklif/kampanya varlığını kendi otoriter DB'sinden
 * doğrulayıp sonucu ({@code CartItemValidated}/{@code CartItemValidationFailed}) geri
 * yayınlar. Değerler cart-service tarafındaki {@code CartSagaEvents} ile birebir aynı olmalıdır.
 */
public final class CartSagaEvents {

    private CartSagaEvents() {
    }

    /** Agrega tipi — Debezium topic'i {@code crm.CartSaga.events}. */
    public static final String AGGREGATE_TYPE = "CartSaga";

    /** cart-service -> : sepete eklenen teklif/kampanya için doğrulama isteği. */
    public static final String ITEM_VALIDATION_REQUESTED = "CartItemValidationRequested";

    /** product-service -> : teklif/kampanya doğrulandı (ad + fiyat + paket içeriği ile). */
    public static final String ITEM_VALIDATED = "CartItemValidated";

    /** product-service -> : teklif/kampanya doğrulanamadı (saga telafi edilmeli). */
    public static final String ITEM_VALIDATION_FAILED = "CartItemValidationFailed";
}
