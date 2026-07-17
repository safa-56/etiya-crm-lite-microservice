package com.etiya.productservice.business.constants;

/**
 * Referans veri sabitleri (Bounded Context Ownership sözleşmesi).
 *
 * <p><b>Sahiplik kuralı:</b> product-service, genel referans tablolarının
 * ({@code general_status}, {@code general_type}) yalnızca aşağıdaki
 * {@code ENTITY_*} dilimlerine sahiptir. {@code PARTY}, {@code CUST_ORD},
 * {@code CUST_ACCT} gibi diğer dilimler ilgili servislerin kendi
 * veritabanlarında durur; burada tutulmaz ve buradan okunmaz.
 *
 * <p><b>Sınır geçiş kuralı:</b> Bir durum/tip servis sınırını geçmesi gerekirse
 * (ör. outbox olay gövdesinde), surrogate id ile değil buradaki {@code *_CODE}
 * sabitleriyle taşınır.
 */
public final class ProductReferenceCodes {

    private ProductReferenceCodes() {
    }

    // --- Sahip olunan dilimler (legacy ENT_CODE_NAME) -------------------------

    /** Ürün dilimi. */
    public static final String ENTITY_PRODUCT = "PROD";

    /** Ürün spesifikasyonu dilimi. */
    public static final String ENTITY_PRODUCT_SPEC = "PROD_SPEC";

    /** Ürün karakteristik değeri dilimi. */
    public static final String ENTITY_PRODUCT_CHAR_VALUE = "PROD_CHAR_VAL";

    /** Ürün spesifikasyonu - servis spesifikasyonu dilimi. */
    public static final String ENTITY_PRODUCT_SPEC_SERVICE_SPEC = "PROD_SPEC_SRVC_SPEC";

    /** Kaynak spesifikasyonu dilimi. */
    public static final String ENTITY_RESOURCE_SPEC = "RSRC_SPEC";

    // --- Durum kısa kodları (legacy GNL_ST.SHRT_CODE) ------------------------

    /** Aktif. */
    public static final String STATUS_ACTIVE_CODE = "ACTV";

    /** Pasif. */
    public static final String STATUS_PASSIVE_CODE = "PASS";

    /** Silinmiş. */
    public static final String STATUS_DELETED_CODE = "DEL";

    /** Beklemede. */
    public static final String STATUS_PENDING_CODE = "PNDG";

    /** Askıda. */
    public static final String STATUS_SUSPENDED_CODE = "SPND";

    /** Sipariş iptal. */
    public static final String STATUS_QUOTE_DELETED_CODE = "QUOTE_DEL";
}
