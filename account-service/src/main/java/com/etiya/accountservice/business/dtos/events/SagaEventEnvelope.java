package com.etiya.accountservice.business.dtos.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Saga olaylarının ortak "zarfı": yalnızca {@code eventType} okunur.
 *
 * <p>Aynı topic'te ({@code crm.BillingAccountSaga.events}) farklı payload tipleri
 * (istek/sonuç) aktığından, tüketici önce bu zarfa ayrıştırıp olay tipine göre
 * doğru somut tipe deserialize eder. Diğer alanlar yok sayılır.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SagaEventEnvelope(String eventType) {
}
