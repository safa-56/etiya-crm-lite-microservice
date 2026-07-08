package com.etiya.accountservice.business.abstracts;

import com.etiya.accountservice.business.dtos.events.CustomerEventPayload;

/**
 * customer-service olaylarını yerel müşteri projeksiyonuna (read-model) uygulayan servis.
 *
 * <p>Bu projeksiyon, fatura hesabı oluşturma/güncelleme tutarlılık kurallarının
 * ("müşteri var mı?", "adres bu müşteriye ait mi?") veri kaynağıdır.
 */
public interface CustomerProjectionService {

    /** Bir müşteri olayını uygular (müşteri ve adres projeksiyonunu günceller). */
    void applyCustomerEvent(CustomerEventPayload payload);
}
