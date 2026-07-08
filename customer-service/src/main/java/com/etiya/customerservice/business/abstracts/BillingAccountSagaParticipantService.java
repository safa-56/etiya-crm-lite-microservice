package com.etiya.customerservice.business.abstracts;

import com.etiya.customerservice.business.dtos.events.BillingAccountSagaRequestedPayload;

/**
 * Fatura hesabı oluşturma Saga'sında customer-service'in doğrulayıcı adımı.
 *
 * <p>account-service'ten gelen doğrulama isteğini işler: müşteri ve adresi
 * otoriter olarak (kendi veritabanından) doğrular ve sonucu (validated/failed)
 * saga kanalına yayınlar.
 */
public interface BillingAccountSagaParticipantService {

    /** Doğrulama isteğini işler ve saga sonucunu yayınlar. */
    void handleCreationRequested(BillingAccountSagaRequestedPayload request);
}
