package com.etiya.productservice.core.constants;

/**
 * Redis cache isimleri için sabitler (magic string kullanılmaz).
 */
public final class CacheNames {

    private CacheNames() {
    }

    /** Tekil ürün teknik özelliği (id -> response) cache'i. */
    public static final String PRODUCT_SPECS = "productSpecs";
    public static final String PRODUCT_SPEC_LIST = "productSpecList";

    /** Tekil ürün teklifi (id -> response) cache'i. */
    public static final String PRODUCT_OFFERS = "productOffers";
    public static final String PRODUCT_OFFER_LIST = "productOfferList";

    /** Tekil katalog (id -> response) cache'i. */
    public static final String CATALOGS = "catalogs";
    public static final String CATALOG_LIST = "catalogList";

    /** Tekil kampanya (id -> response) cache'i. */
    public static final String CAMPAIGNS = "campaigns";
    public static final String CAMPAIGN_LIST = "campaignList";

    /** Tekil ürün (id -> response) cache'i. */
    public static final String PRODUCTS = "products";
}
