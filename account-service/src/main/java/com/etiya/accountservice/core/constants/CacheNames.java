package com.etiya.accountservice.core.constants;

/**
 * Redis cache isimleri için sabitler (magic string kullanılmaz).
 */
public final class CacheNames {

    private CacheNames() {
    }

    /** Tekil fatura hesabı (id -> response) cache'i. */
    public static final String BILLING_ACCOUNTS = "billingAccounts";

    /** Fatura hesabı liste cache'i. */
    public static final String BILLING_ACCOUNT_LIST = "billingAccountList";
}
