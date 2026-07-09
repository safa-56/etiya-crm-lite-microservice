package com.etiya.cartservice.business.constants;

/**
 * Sepete ekleme <b>Saga</b>'sının (choreography) olay/kanal sabitleri.
 *
 * <p>Saga tek bir mantıksal kanal üzerinden yürür: aggregate tipi {@code CartSaga}
 * olan tüm outbox kayıtları — hangi servisin DB'sinden gelirse gelsin — Debezium
 * EventRouter ile {@code crm.CartSaga.events} topic'ine yönlendirilir. Hem cart-service
 * hem product-service bu topic'i dinler ve yalnızca kendini ilgilendiren
 * {@code eventType}'ları işler (kendi ürettiği olayları atlar → döngü yok).
 *
 * <p>Akış:
 * <ol>
 *   <li>cart-service: {@link #ITEM_VALIDATION_REQUESTED} (sepet satırı PENDING açılır).</li>
 *   <li>product-service: {@link #ITEM_VALIDATED} (ad/fiyat snapshot'ıyla) ya da
 *       {@link #ITEM_VALIDATION_FAILED} (neden ile).</li>
 *   <li>cart-service: onay (ACTIVE) ya da telafi (CANCELLED + soft-delete).</li>
 * </ol>
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
