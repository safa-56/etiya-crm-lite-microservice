package com.etiya.accountservice.business.constants;

/**
 * Outbox olayları için sabitler (aggregate tipi ve olay tipleri).
 * Magic string kullanılmaz; Debezium EventRouter yönlendirmesi bu değerlere dayanır.
 */
public final class AccountEvents {

    private AccountEvents() {
    }

    /** Agrega tipi — Debezium {@code route.by.field} değeri; topic: {@code crm.Account.events}. */
    public static final String AGGREGATE_TYPE = "Account";

    public static final String BILLING_ACCOUNT_CREATED = "BillingAccountCreated";
    public static final String BILLING_ACCOUNT_UPDATED = "BillingAccountUpdated";
    public static final String BILLING_ACCOUNT_DELETED = "BillingAccountDeleted";
}
