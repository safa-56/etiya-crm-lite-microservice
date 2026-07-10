package com.etiya.orderservice.core.constants;

/**
 * Redis cache isimleri için sabitler (magic string kullanılmaz).
 */
public final class CacheNames {

    private CacheNames() {
    }

    /** Tekil sipariş (id -> response) cache'i. */
    public static final String ORDERS = "orders";

    /** Sipariş liste cache'i. */
    public static final String ORDER_LIST = "orderList";
}
