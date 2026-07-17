package com.etiya.accountservice.business.constants;

/**
 * Referans veri sabitleri (Bounded Context Ownership sözleşmesi).
 *
 * <p><b>Sahiplik kuralı:</b> account-service, genel referans tablolarının
 * ({@code general_status}, {@code general_type}) yalnızca {@code CUST_ACCT} ve
 * {@code CUST_ACCT_PROD_INVL} dilimlerine sahiptir. {@code PARTY}, {@code PROD},
 * {@code CUST_ORD} gibi diğer dilimler ilgili servislerin kendi veritabanlarında
 * durur.
 *
 * <p><b>Sınır geçiş kuralı:</b> Bir durum/tip servis sınırını geçmesi gerekirse
 * (ör. outbox olay gövdesinde), surrogate id ile değil buradaki {@code *_CODE}
 * sabitleriyle taşınır.
 */
public final class AccountReferenceCodes {

    private AccountReferenceCodes() {
    }

    // --- Sahip olunan dilimler (legacy ENT_CODE_NAME) -------------------------

    /** Müşteri (fatura) hesabı dilimi. */
    public static final String ENTITY_CUSTOMER_ACCOUNT = "CUST_ACCT";

    /**
     * Müşteri hesabı - ürün ilişkisi dilimi.
     *
     * <p>Not: Bu entity'nin ({@code CUST_ACCT_PROD_INVL}) mevcut modelimizde bir
     * karşılığı yoktur; dilim yalnızca legacy/ETL hizası için taşınır.
     */
    public static final String ENTITY_CUSTOMER_ACCOUNT_PRODUCT_INVOLVEMENT = "CUST_ACCT_PROD_INVL";

    // --- Durum kısa kodları (legacy GNL_ST.SHRT_CODE) ------------------------

    /** Aktif. */
    public static final String STATUS_ACTIVE_CODE = "ACTV";

    /** Silinmiş. */
    public static final String STATUS_DELETED_CODE = "DEL";

    /** Beklemede. */
    public static final String STATUS_PENDING_CODE = "PNDG";

    /** İptal edilmiş. */
    public static final String STATUS_CANCELLED_CODE = "CNCL";

    /** Askıda. */
    public static final String STATUS_SUSPENDED_CODE = "SPND";
}
