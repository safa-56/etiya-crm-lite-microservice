package com.etiya.accountservice.core.constants;

/**
 * Redis cache isimleri için sabitler (magic string kullanılmaz).
 */
public final class CacheNames {

    private CacheNames() {
    }

    /** Tekil fatura hesabı (id -> response) cache'i. */
    public static final String BILLING_ACCOUNTS = "billingAccounts";

    /** Sayfalı fatura hesabı liste cache'i (getAll -> {@code PagedResponse}). */
    public static final String BILLING_ACCOUNT_LIST = "billingAccountList";

    /**
     * Bir müşteriye bağlı fatura hesapları cache'i (getByCustomer -> {@code List}).
     * Sayfalı liste cache'inden ayrıdır: her cache tek somut tip tuttuğundan tipe-bağlı
     * serializer kullanılabilir (global default typing'in List round-trip sorunundan kaçınılır).
     */
    public static final String BILLING_ACCOUNTS_BY_CUSTOMER = "billingAccountsByCustomer";
}
