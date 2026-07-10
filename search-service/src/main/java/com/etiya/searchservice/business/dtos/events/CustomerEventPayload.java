package com.etiya.searchservice.business.dtos.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;

/**
 * customer-service'in {@code crm.Customer.events} akışında yayınladığı müşteri
 * olayının, search-service tarafındaki tüketici gösterimi.
 *
 * <p>Yalnızca aramanın ihtiyaç duyduğu alanlar tutulur; olay sözleşmesi ileride
 * genişlerse bilinmeyen alanlar yok sayılır ({@code @JsonIgnoreProperties}).
 * {@code addresses} alanı bu servis için gereksiz olduğundan modellenmemiştir
 * (JSON'da varsa göz ardı edilir).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CustomerEventPayload(
        Long customerId,
        String firstName,
        String secondName,
        String lastName,
        String nationalityId,
        String gsmNumber,
        String role,
        String eventType,
        LocalDateTime occurredAt
) {
}
