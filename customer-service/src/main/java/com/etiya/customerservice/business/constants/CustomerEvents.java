package com.etiya.customerservice.business.constants;

/**
 * Outbox olayları için sabitler (aggregate tipi ve olay tipleri).
 * Magic string kullanılmaz; Debezium EventRouter yönlendirmesi bu değerlere dayanır.
 */
public final class CustomerEvents {

    private CustomerEvents() {
    }

    /** Agrega tipi — Debezium {@code route.by.field} değeri; topic: {@code crm.Customer.events}. */
    public static final String AGGREGATE_TYPE = "Customer";

    public static final String CUSTOMER_CREATED = "CustomerCreated";
    public static final String CUSTOMER_UPDATED = "CustomerUpdated";
    public static final String CUSTOMER_DELETED = "CustomerDeleted";
}
