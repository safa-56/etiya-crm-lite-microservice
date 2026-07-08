package com.etiya.accountservice.business.constants;

/**
 * product-service'ten tüketilen ürün olay tipleri için sabitler (magic string yok).
 *
 * <p>product-service henüz yazılmadığından bu değerler beklenen sözleşmedir;
 * hesaba bağlı aktif ürün sayısını ({@code active_product_count}) güncel tutmak
 * için kullanılır (silme kuralının dayanağı).
 */
public final class ProductEvents {

    private ProductEvents() {
    }

    /** Yeni/aktif ürün — hesabın aktif ürün sayısını artırır. */
    public static final String PRODUCT_CREATED = "ProductCreated";
    public static final String PRODUCT_ACTIVATED = "ProductActivated";

    /** Kaldırılan/pasifleşen ürün — hesabın aktif ürün sayısını azaltır. */
    public static final String PRODUCT_DELETED = "ProductDeleted";
    public static final String PRODUCT_DEACTIVATED = "ProductDeactivated";
}
