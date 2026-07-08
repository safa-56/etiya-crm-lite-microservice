package com.etiya.accountservice.business.constants;

/**
 * customer-service'ten tüketilen müşteri olay tipleri ve topic yönlendirme sabitleri
 * (magic string kullanılmaz).
 *
 * <p>Değerler, customer-service üretici tarafıyla ({@code CustomerEvents})
 * birebir aynı olmalıdır; aksi halde projeksiyon güncellemesi tetiklenmez.
 */
public final class CustomerEvents {

    private CustomerEvents() {
    }

    /** Agrega tipi — Debezium topic'i {@code crm.Customer.events}. */
    public static final String AGGREGATE_TYPE = "Customer";

    public static final String CUSTOMER_CREATED = "CustomerCreated";
    public static final String CUSTOMER_UPDATED = "CustomerUpdated";
    public static final String CUSTOMER_DELETED = "CustomerDeleted";
}
