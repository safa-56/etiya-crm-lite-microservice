package com.etiya.cartservice.business.constants;

/**
 * Sepetten siparişe geçiş (checkout) <b>Saga</b>'sının (choreography) olay/kanal sabitleri —
 * cart-service <b>doğrulayıcı (participant)</b> tarafı.
 *
 * <p>Saga tek bir mantıksal kanal üzerinden yürür: aggregate tipi {@code OrderCheckoutSaga}
 * olan tüm outbox kayıtları — hangi servisin DB'sinden gelirse gelsin — Debezium
 * EventRouter ile {@code crm.OrderCheckoutSaga.events} topic'ine yönlendirilir. Hem
 * order-service hem cart-service bu topic'i dinler ve yalnızca kendini ilgilendiren
 * {@code eventType}'ları işler (kendi ürettiği olayları atlar → döngü yok).
 *
 * <p>cart-service bu saga'da <b>doğrulayıcı</b>dır: order-service'in
 * {@link #CHECKOUT_REQUESTED} isteğini alır, sepeti otoriter kontrol eder ve
 * {@link #CART_VALIDATED} (satır/toplam snapshot'ıyla) ya da {@link #CART_VALIDATION_FAILED}
 * (neden ile) yayınlar.
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
