package com.etiya.searchservice.business.constants;

/**
 * Arama format/validasyon mesaj <b>anahtarları</b> (i18n).
 *
 * <p>Format mesajları FR-002 kabul kriterlerinden (ACC-04..10) alınmıştır. Gerçek
 * metin isteğin diline göre {@code messages*.properties} üzerinden çözülür;
 * İngilizce değerler UI ile birebir aynı metinleri korur.
 */
public final class Messages {

    private Messages() {
    }

    /** ACC-04: ID Number yalnızca 11 haneli rakam. */
    public static final String INVALID_ID_NUMBER = "search.idNumber.invalid";

    /** ACC-05: GSM Number yalnızca rakam, en fazla 15 karakter. */
    public static final String INVALID_GSM_NUMBER = "search.gsmNumber.invalid";

    /** ACC-06: Customer ID yalnızca rakam, en fazla 20 karakter. */
    public static final String INVALID_CUSTOMER_ID = "search.customerId.invalid";

    /** ACC-07: Account Number alfanümerik, en fazla 30 karakter. */
    public static final String INVALID_ACCOUNT_NUMBER = "search.accountNumber.invalid";

    /** ACC-08: Order Number alfanümerik, en fazla 20 karakter. */
    public static final String INVALID_ORDER_NUMBER = "search.orderNumber.invalid";

    /** ACC-09: First Name / Last Name en fazla 50 karakter. */
    public static final String INVALID_NAME_LENGTH = "search.nameLength.invalid";

    /** Ad/soyad alanında harf dışı karakter var. */
    public static final String INVALID_NAME_PATTERN = "search.namePattern.invalid";
}
