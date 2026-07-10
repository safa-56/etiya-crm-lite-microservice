package com.etiya.searchservice.business.constants;

/**
 * İş/validasyon mesajları için sabitler (magic string kullanılmaz).
 *
 * <p>Format mesajları FR-002 kabul kriterlerinden (ACC-04..10) alınmıştır; UI ile
 * birebir aynı metinler kullanılır.
 */
public final class Messages {

    private Messages() {
    }

    /** ACC-04: ID Number yalnızca 11 haneli rakam. */
    public static final String INVALID_ID_NUMBER = "Please enter a valid 11-digit ID number.";

    /** ACC-05: GSM Number yalnızca rakam, en fazla 15 karakter. */
    public static final String INVALID_GSM_NUMBER = "Please enter a valid GSM number.";

    /** ACC-06: Customer ID yalnızca rakam, en fazla 20 karakter. */
    public static final String INVALID_CUSTOMER_ID = "Please enter a valid customer ID (digits only, max 20).";

    /** ACC-07: Account Number alfanümerik, en fazla 30 karakter. */
    public static final String INVALID_ACCOUNT_NUMBER = "Please enter a valid account number (alphanumeric, max 30).";

    /** ACC-08: Order Number alfanümerik, en fazla 20 karakter. */
    public static final String INVALID_ORDER_NUMBER = "Please enter a valid order number (alphanumeric, max 20).";

    /** ACC-09: First Name / Last Name en fazla 50 karakter. */
    public static final String INVALID_NAME_LENGTH = "First name and last name must be at most 50 characters.";
}
