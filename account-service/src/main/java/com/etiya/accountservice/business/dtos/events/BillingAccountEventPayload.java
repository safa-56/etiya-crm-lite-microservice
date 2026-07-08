package com.etiya.accountservice.business.dtos.events;

import com.etiya.accountservice.entities.enums.AccountStatus;

import java.time.LocalDateTime;

/**
 * Outbox'a yazılan ve Debezium ile Kafka'ya yayınlanan fatura hesabı olayı gövdesi.
 * Diğer servisler (ör. product, notification) bu olayı tüketir.
 */
public record BillingAccountEventPayload(
        Long billingAccountId,
        Long customerId,
        String accountName,
        AccountStatus accountStatus,
        LocalDateTime occurredAt
) {
}
