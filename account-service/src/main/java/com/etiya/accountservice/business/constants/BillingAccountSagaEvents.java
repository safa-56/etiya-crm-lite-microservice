package com.etiya.accountservice.business.constants;

/**
 * Fatura hesabı oluşturma <b>Saga</b>'sının (choreography) olay/kanal sabitleri.
 *
 * <p>Saga, tek bir mantıksal kanal üzerinden yürür: aggregate tipi
 * {@code BillingAccountSaga} olan tüm outbox kayıtları — hangi servisin DB'sinden
 * gelirse gelsin — Debezium EventRouter ile {@code crm.BillingAccountSaga.events}
 * topic'ine yönlendirilir. Hem account-service hem customer-service bu topic'i
 * dinler ve yalnızca kendini ilgilendiren {@code eventType}'ları işler.
 *
 * <p>Akış:
 * <ol>
 *   <li>account-service: {@link #CREATION_REQUESTED} (hesap PENDING).</li>
 *   <li>customer-service: {@link #CUSTOMER_VALIDATED} / {@link #CUSTOMER_VALIDATION_FAILED}.</li>
 *   <li>account-service: onay (ACTIVE) ya da telafi (CANCELLED).</li>
 * </ol>
 */
public final class BillingAccountSagaEvents {

    private BillingAccountSagaEvents() {
    }

    /** Agrega tipi — Debezium topic'i {@code crm.BillingAccountSaga.events}. */
    public static final String AGGREGATE_TYPE = "BillingAccountSaga";

    /** account-service -> : yeni fatura hesabı için müşteri/adres doğrulaması isteği. */
    public static final String CREATION_REQUESTED = "BillingAccountCreationRequested";

    /** customer-service -> : müşteri ve adres doğrulandı (saga ileri gidebilir). */
    public static final String CUSTOMER_VALIDATED = "BillingAccountCustomerValidated";

    /** customer-service -> : müşteri/adres doğrulanamadı (saga telafi edilmeli). */
    public static final String CUSTOMER_VALIDATION_FAILED = "BillingAccountCustomerValidationFailed";
}
