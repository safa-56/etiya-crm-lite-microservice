package com.etiya.cartservice.core.constants;

/**
 * Redis cache isimleri için sabitler (magic string kullanılmaz).
 */
public final class CacheNames {

    private CacheNames() {
    }

    /** Tekil sepet (id -> response) cache'i. */
    public static final String CARTS = "carts";

    /** Sepet liste cache'i. */
    public static final String CART_LIST = "cartList";
}
