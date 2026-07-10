package com.etiya.searchservice.core.constants;

/**
 * Redis cache isimleri için sabitler (magic string kullanılmaz).
 */
public final class CacheNames {

    private CacheNames() {
    }

    /** Müşteri arama sonucu (parametre bazlı) liste cache'i. */
    public static final String CUSTOMER_SEARCH = "customerSearch";
}
