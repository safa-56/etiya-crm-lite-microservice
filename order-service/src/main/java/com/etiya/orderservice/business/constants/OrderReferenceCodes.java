package com.etiya.orderservice.business.constants;

/**
 * Referans veri sabitleri (Bounded Context Ownership sözleşmesi).
 *
 * <p><b>Sahiplik kuralı:</b> order-service, genel referans tablolarının
 * ({@code general_status}, {@code general_type}) yalnızca {@code CUST_ORD}
 * dilimine sahiptir. {@code PARTY}, {@code PROD}, {@code CUST_ACCT} gibi diğer
 * dilimler ilgili servislerin kendi veritabanlarında durur.
 *
 * <p><b>Sınır geçiş kuralı:</b> Bir durum/tip servis sınırını geçmesi gerekirse
 * (ör. outbox olay gövdesinde), surrogate id ile değil buradaki {@code *_CODE}
 * sabitleriyle taşınır.
 */
public final class OrderReferenceCodes {

    private OrderReferenceCodes() {
    }

    // --- Sahip olunan dilim (legacy ENT_CODE_NAME) ---------------------------

    /** Müşteri siparişi dilimi. */
    public static final String ENTITY_CUSTOMER_ORDER = "CUST_ORD";

    // --- Durum kısa kodları (legacy GNL_ST.SHRT_CODE, CUST_ORD dilimi) -------

    /** Sipariş alındı, işleniyor. */
    public static final String STATUS_IN_PROGRESS_CODE = "MIDLWARE";

    /** Tamamlandı. */
    public static final String STATUS_FINISHED_CODE = "FINISHED";

    /** Reddedildi. */
    public static final String STATUS_REJECTED_CODE = "REJECTED";
}
