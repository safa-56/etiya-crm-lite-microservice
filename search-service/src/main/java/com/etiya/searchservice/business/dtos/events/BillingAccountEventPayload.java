package com.etiya.searchservice.business.dtos.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * account-service'in {@code crm.Account.events} akışında yayınladığı fatura
 * hesabı olayının, search-service tarafındaki tüketici gösterimi.
 *
 * <p>{@code accountStatus} burada {@code String} olarak tutulur (kaynak servisin
 * {@code AccountStatus} enum'una bağımlılık kurulmaz); ekleme/çıkarma kararı bu
 * duruma göre verilir: {@code ACTIVE} → numaraları ekle, {@code CANCELLED}/
 * {@code PASSIVE} → çıkar. Bilinmeyen alanlar yok sayılır.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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
