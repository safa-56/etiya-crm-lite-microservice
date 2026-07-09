package com.etiya.productservice.business.constants;

/**
 * Ürün satışı <b>Saga</b>'sının (choreography) olay/kanal sabitleri.
 *
 * <p>Saga, tek bir mantıksal kanal üzerinden yürür: aggregate tipi
 * {@code ProductSaga} olan tüm outbox kayıtları — hangi servisin DB'sinden gelirse
 * gelsin — Debezium EventRouter ile {@code crm.ProductSaga.events} topic'ine
 * yönlendirilir. Hem product-service hem account-service bu topic'i dinler ve
 * yalnızca kendini ilgilendiren {@code eventType}'ları işler.
 *
 * <p>Akış:
 * <ol>
 *   <li>product-service: {@link #SALE_REQUESTED} (ürün PENDING).</li>
 *   <li>account-service: {@link #ACCOUNT_VALIDATED} / {@link #ACCOUNT_VALIDATION_FAILED}.</li>
 *   <li>product-service: onay (ACTIVE) ya da telafi (CANCELLED).</li>
 * </ol>
 */
public final class ProductSagaEvents {

    private ProductSagaEvents() {
    }

    /** Agrega tipi — Debezium topic'i {@code crm.ProductSaga.events}. */
    public static final String AGGREGATE_TYPE = "ProductSaga";

    /** product-service -> : yeni ürün için fatura hesabı doğrulaması isteği. */
    public static final String SALE_REQUESTED = "ProductSaleRequested";

    /** account-service -> : fatura hesabı doğrulandı (saga ileri gidebilir). */
    public static final String ACCOUNT_VALIDATED = "ProductAccountValidated";

    /** account-service -> : fatura hesabı doğrulanamadı (saga telafi edilmeli). */
    public static final String ACCOUNT_VALIDATION_FAILED = "ProductAccountValidationFailed";
}
