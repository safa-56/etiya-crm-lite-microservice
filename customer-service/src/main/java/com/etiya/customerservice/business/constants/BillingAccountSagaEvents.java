package com.etiya.customerservice.business.constants;

/**
 * Fatura hesabı oluşturma <b>Saga</b>'sının (choreography) customer-service
 * tarafındaki olay/kanal sabitleri. Değerler account-service ile birebir aynıdır.
 *
 * <p>customer-service bu saga'da <b>doğrulayıcı (participant)</b> rolündedir:
 * {@link #CREATION_REQUESTED} olayını dinler, müşteri/adresi otoriter olarak
 * kendi veritabanından doğrular ve {@link #CUSTOMER_VALIDATED} ya da
 * {@link #CUSTOMER_VALIDATION_FAILED} sonucunu yayınlar. Tüm bu olaylar
 * {@code crm.BillingAccountSaga.events} topic'i üzerinden akar.
 */
public final class BillingAccountSagaEvents {

    private BillingAccountSagaEvents() {
    }

    /** Agrega tipi — Debezium topic'i {@code crm.BillingAccountSaga.events}. */
    public static final String AGGREGATE_TYPE = "BillingAccountSaga";

    /** account-service -> : yeni fatura hesabı için müşteri/adres doğrulaması isteği. */
    public static final String CREATION_REQUESTED = "BillingAccountCreationRequested";

    /** account-service -> : mevcut hesabın adres değişikliği için doğrulama isteği. */
    public static final String ADDRESS_CHANGE_REQUESTED = "BillingAccountAddressChangeRequested";

    /** customer-service -> : müşteri ve adres doğrulandı. */
    public static final String CUSTOMER_VALIDATED = "BillingAccountCustomerValidated";

    /** customer-service -> : müşteri/adres doğrulanamadı. */
    public static final String CUSTOMER_VALIDATION_FAILED = "BillingAccountCustomerValidationFailed";
}
