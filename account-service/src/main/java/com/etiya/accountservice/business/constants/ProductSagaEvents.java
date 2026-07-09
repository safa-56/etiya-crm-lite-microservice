package com.etiya.accountservice.business.constants;

/**
 * Ürün satışı <b>Saga</b>'sının account-service (doğrulayıcı) tarafındaki
 * olay/kanal sabitleri.
 *
 * <p>Saga tek kanaldan yürür: aggregate tipi {@code ProductSaga} olan outbox
 * kayıtları Debezium EventRouter ile {@code crm.ProductSaga.events} topic'ine
 * yönlendirilir. product-service istekleri ({@link #SALE_REQUESTED}) gönderir;
 * account-service fatura hesabını doğrulayıp sonucu ({@link #ACCOUNT_VALIDATED} /
 * {@link #ACCOUNT_VALIDATION_FAILED}) yayınlar.
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
