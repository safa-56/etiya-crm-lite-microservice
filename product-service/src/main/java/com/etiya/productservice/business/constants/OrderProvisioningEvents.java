package com.etiya.productservice.business.constants;

/**
 * order-service'in yayınladığı sipariş entegrasyon olaylarının tüketici tarafı sabitleri.
 *
 * <p>order-service, bir sipariş CONFIRMED olduğunda {@code crm.Order.events} kanalına
 * {@link #ORDER_CONFIRMED} olayını yayınlar. product-service bu olayı tüketip sipariş
 * kalemlerinden {@code Product} kayıtları üretir (provizyon). İki servis birbirini
 * doğrudan çağırmaz; akış tamamen olay-tabanlıdır.
 */
public final class OrderProvisioningEvents {

    private OrderProvisioningEvents() {
    }

    /** order-service -> : sipariş kesinleşti; kalemleri provizyone edilebilir. */
    public static final String ORDER_CONFIRMED = "OrderConfirmed";

    /** Provizyon kalem türü: katalogdan seçilmiş tek teklif. */
    public static final String ITEM_TYPE_OFFER = "OFFER";

    /** Provizyon kalem türü: paket (bundle) kampanya. */
    public static final String ITEM_TYPE_CAMPAIGN = "CAMPAIGN";
}
