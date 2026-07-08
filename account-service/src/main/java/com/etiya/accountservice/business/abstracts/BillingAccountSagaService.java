package com.etiya.accountservice.business.abstracts;

import com.etiya.accountservice.business.dtos.events.BillingAccountSagaValidationPayload;

/**
 * Fatura hesabı oluşturma Saga'sının account-service tarafındaki adımı:
 * customer-service'ten gelen doğrulama sonucunu uygular (onay ya da telafi).
 */
public interface BillingAccountSagaService {

    /**
     * Doğrulama sonucunu uygular:
     * <ul>
     *   <li>başarılı: PENDING hesabı ACTIVE yapar, otoriter adres snapshot'ını yazar;</li>
     *   <li>başarısız: hesabı CANCELLED yapar (telafi/compensation).</li>
     * </ul>
     * PENDING olmayan hesaplarda idempotent olarak atlanır.
     */
    void applyValidationResult(BillingAccountSagaValidationPayload payload);
}
