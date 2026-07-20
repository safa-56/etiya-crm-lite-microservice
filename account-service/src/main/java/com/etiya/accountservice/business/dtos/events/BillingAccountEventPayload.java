package com.etiya.accountservice.business.dtos.events;

import java.time.LocalDateTime;

/**
 * Outbox'a yazılan ve Debezium ile Kafka'ya yayınlanan fatura hesabı olayı gövdesi.
 * Diğer servisler (ör. product, notification, search) bu olayı tüketir.
 *
 * <p>{@code accountNumber} ve {@code orderNumber}, müşteri arama read-model'i
 * (search-service, FR-002) için taşınır: arama ekranındaki Account Number / Order
 * Number tam-eşleşme kriterleri bu alanlardan beslenir.
 *
 * <p>{@code accountStatus} servis sınırını <b>stabil kısa kod</b> (general_status
 * shortCode, ör. {@code ACTV}/{@code CNCL}/{@code DEL}) olarak geçer; tüketiciler
 * (search-service) bu koda göre ekleme/çıkarma kararı verir.
 */
public record BillingAccountEventPayload(
        Long billingAccountId,
        Long customerId,
        String accountName,
        String accountNumber,
        String orderNumber,
        String accountStatus,
        LocalDateTime occurredAt
) {
}
