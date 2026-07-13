package com.etiya.orderservice.business.constants;

/**
 * Sipariş entegrasyon olaylarının (saga-dışı) aggregate/olay sabitleri.
 *
 * <p>Sepetten siparişe geçiş Saga'sı ({@link OrderCheckoutSagaEvents}) siparişi
 * CONFIRMED yaptıktan sonra, ürün <b>provizyonunu</b> tetiklemek için bu kanaldan
 * ayrı bir entegrasyon olayı yayınlanır. Aggregate tipi {@code Order} olan outbox
 * kayıtları Debezium EventRouter ile {@code crm.Order.events} topic'ine yönlendirilir;
 * bu topic'i product-service dinleyerek sipariş kalemlerinden {@code Product}
 * kayıtlarını üretir (choreography — sipariş domaini product domainini doğrudan
 * çağırmaz).
 *
 * <p>Bu, Product Sale Saga'sı ({@code crm.ProductSaga.events}) ile karıştırılmamalıdır:
 * burada order-service yalnızca "şu sipariş kesinleşti, şu kalemler provizyone edilebilir"
 * bilgisini yayınlar; ürünün fatura hesabına karşı doğrulanması ve aktif ürün sayacının
 * güncellenmesi product-service'in başlattığı ayrı saga akışında olur.
 */
public final class OrderEvents {

    private OrderEvents() {
    }

    /** Agrega tipi — Debezium {@code route.by.field} değeri; topic: {@code crm.Order.events}. */
    public static final String AGGREGATE_TYPE = "Order";

    /** order-service -> : sipariş kesinleşti (CONFIRMED); kalemleri provizyone edilebilir. */
    public static final String ORDER_CONFIRMED = "OrderConfirmed";
}
