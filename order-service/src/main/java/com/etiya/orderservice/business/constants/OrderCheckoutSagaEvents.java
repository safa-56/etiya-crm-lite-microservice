package com.etiya.orderservice.business.constants;

/**
 * Sepetten siparişe geçiş (checkout) <b>Saga</b>'sının (choreography) olay/kanal sabitleri.
 *
 * <p>Saga tek bir mantıksal kanal üzerinden yürür: aggregate tipi {@code OrderCheckoutSaga}
 * olan tüm outbox kayıtları — hangi servisin DB'sinden gelirse gelsin — Debezium
 * EventRouter ile {@code crm.OrderCheckoutSaga.events} topic'ine yönlendirilir. Hem
 * order-service hem cart-service bu topic'i dinler ve yalnızca kendini ilgilendiren
 * {@code eventType}'ları işler (kendi ürettiği olayları atlar → döngü yok).
 *
 * <p>Akış (FR-016):
 * <ol>
 *   <li>order-service: {@link #CHECKOUT_REQUESTED} (sipariş PENDING açılır).</li>
 *   <li>cart-service: {@link #CART_VALIDATED} (satır/toplam snapshot'ıyla) ya da
 *       {@link #CART_VALIDATION_FAILED} (neden ile).</li>
 *   <li>order-service: onay (CONFIRMED) ya da telafi (CANCELLED + soft-delete).</li>
 * </ol>
 */
public final class OrderCheckoutSagaEvents {

    private OrderCheckoutSagaEvents() {
    }

    /** Agrega tipi — Debezium topic'i {@code crm.OrderCheckoutSaga.events}. */
    public static final String AGGREGATE_TYPE = "OrderCheckoutSaga";

    /** order-service -> : bir sepet submit edildi, sepet doğrulaması isteniyor. */
    public static final String CHECKOUT_REQUESTED = "OrderCheckoutRequested";

    /** cart-service -> : sepet doğrulandı (satırlar + toplam + sahiplik snapshot'ı ile). */
    public static final String CART_VALIDATED = "OrderCartValidated";

    /** cart-service -> : sepet doğrulanamadı (yok/boş) — saga telafi edilmeli. */
    public static final String CART_VALIDATION_FAILED = "OrderCartValidationFailed";
}
