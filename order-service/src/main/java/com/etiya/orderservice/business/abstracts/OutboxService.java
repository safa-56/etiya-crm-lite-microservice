package com.etiya.orderservice.business.abstracts;

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
     * @param aggregateType agrega tipi (topic yönlendirmesi) — ör. "OrderCheckoutSaga"
     * @param aggregateId   agrega kimliği (mesaj anahtarı) — ör. sipariş id'si
     * @param eventType     olay tipi — ör. "OrderCheckoutRequested"
     * @param payload       olay gövdesi (JSON'a serialize edilir)
     */
    void publish(String aggregateType, String aggregateId, String eventType, Object payload);
}
