package com.etiya.searchservice.business.abstracts;

import com.etiya.searchservice.business.dtos.events.BillingAccountEventPayload;
import com.etiya.searchservice.business.dtos.events.CustomerEventPayload;

/**
 * Müşteri arama indeksinin (read-model) olaylardan beslenmesi — projeksiyon tarafı.
 *
 * <p>Consumer'lar tarafından çağrılır; upsert/remove mantığını taşır. İki akış
 * (customer + account) birbirinden bağımsız sırada gelebilir (bkz. stub satır
 * senaryosu).
 */
public interface CustomerSearchIndexService {

    /**
     * Müşteri olayını uygular: create/update → indeks satırını {@code customerId}'ye
     * göre upsert eder (yalnızca dolu alanlar); delete → satırı kaldırır.
     */
    void applyCustomerEvent(CustomerEventPayload event);

    /**
     * Fatura hesabı olayını uygular: hesap aktifse account/order numaralarını ilgili
     * müşteri satırına ekler (satır yoksa stub oluşturur); iptal/pasifse çıkarır.
     */
    void applyBillingAccountEvent(BillingAccountEventPayload event);
}
