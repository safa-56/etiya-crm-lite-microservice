package com.etiya.customerservice.business.abstracts;

/**
 * Transactional Outbox yazma servisi.
 *
 * <p>Çağıran manager'ın {@code @Transactional} sınırı içinde çalıştırılmalıdır;
 * böylece olay kaydı iş verisiyle atomik olarak commit edilir (ghost event yok).
 */
public interface OutboxService {

    /**
     * Bir domain olayını outbox tablosuna yazar.
     *
     * @param aggregateType agrega tipi (topic yönlendirmesi) — ör. "Customer"
     * @param aggregateId   agrega kimliği (mesaj anahtarı) — ör. müşteri id'si
     * @param eventType     olay tipi — ör. "CustomerCreated"
     * @param payload       olay gövdesi (JSON'a serialize edilir)
     */
    void publish(String aggregateType, String aggregateId, String eventType, Object payload);
}
