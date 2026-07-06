package com.etiya.customerservice.core.constants;

/**
 * Redis cache isimleri için sabitler (magic string kullanılmaz).
 */
public final class CacheNames {

    private CacheNames() {
    }

    /** Tekil bireysel müşteri (id -> response) cache'i. */
    public static final String INDIVIDUAL_CUSTOMERS = "individualCustomers";

    /** Bireysel müşteri liste cache'i. */
    public static final String INDIVIDUAL_CUSTOMER_LIST = "individualCustomerList";
}
