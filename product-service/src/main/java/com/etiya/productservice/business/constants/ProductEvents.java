package com.etiya.productservice.business.constants;

/**
 * Outbox olayları için sabitler (aggregate tipi ve olay tipleri).
 *
 * <p>Magic string kullanılmaz; Debezium EventRouter yönlendirmesi ({@code route.by.field})
 * bu değerlere dayanır. Bu olaylar {@code crm.Product.events} topic'ine yayınlanır
 * ve account-service tarafından tüketilerek fatura hesabının aktif ürün sayısı
 * ({@code active_product_count}) güncellenir.
 */
public final class ProductEvents {

    private ProductEvents() {
    }

    /** Agrega tipi — Debezium {@code route.by.field} değeri; topic: {@code crm.Product.events}. */
    public static final String AGGREGATE_TYPE = "Product";

    /** Yeni satılan ürün — hesabın aktif ürün sayısını artırır. */
    public static final String PRODUCT_CREATED = "ProductCreated";

    /** Kaldırılan (soft-delete) ürün — hesabın aktif ürün sayısını azaltır. */
    public static final String PRODUCT_DELETED = "ProductDeleted";
}
