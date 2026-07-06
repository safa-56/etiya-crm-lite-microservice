package com.etiya.customerservice.business.dtos.events;

import java.time.LocalDateTime;

/**
 * Outbox'a yazılan ve Debezium ile Kafka Cloud'a yayınlanan müşteri olayı gövdesi.
 * Diğer servisler (ör. account, notification) bu olayı tüketir.
 */
public record CustomerEventPayload(
        Long customerId,
        String firstName,
        String lastName,
        LocalDateTime occurredAt
) {
}
