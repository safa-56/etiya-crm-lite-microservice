package com.etiya.customerservice.business.dtos.events;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbox'a yazılan ve Debezium ile Kafka'ya ({@code crm.Customer.events})
 * yayınlanan müşteri olayı gövdesi. Diğer servisler (ör. account-service) bu
 * olayı tüketerek kendi yerel <b>müşteri projeksiyonunu</b> (read-model) güncel
 * tutar.
 *
 * <p>account-service, fatura hesabı CRUD'unda tutarlılık için müşteri kimliğine
 * ({@code customerId}) ve müşterinin adreslerine ({@code addresses}) ihtiyaç
 * duyar; bu nedenle olay bu verileri taşır. {@code eventType}, Debezium
 * EventRouter payload'ı ayrıştırmadan da ayrı bir header üretmediğinden, olay
 * tipini tüketici tarafına taşımak için gövdeye eklenmiştir (product olay
 * sözleşmesiyle aynı yaklaşım).
 *
 * <p>{@code secondName}, {@code nationalityId} (TCKN), {@code gsmNumber} ve
 * {@code role}, müşteri arama read-model'i (search-service, FR-002) için eklenmiştir;
 * arama sonuç kolonları ve tam-eşleşme kriterleri bu alanlardan beslenir.
 * {@code gsmNumber}, müşterinin birincil (ilk aktif) iletişim bilgisindeki GSM'dir;
 * {@code role} şimdilik sabit "B2C"dir (yalnızca bireysel müşteri var).
 */
public record CustomerEventPayload(
        Long customerId,
        String firstName,
        String secondName,
        String lastName,
        String nationalityId,
        String gsmNumber,
        String role,
        String eventType,
        List<AddressPayload> addresses,
        LocalDateTime occurredAt
) {

    /**
     * Müşteriye ait tek bir adresin olay gövdesindeki gösterimi. account-service
     * bu bilgiyi {@code customer_address_projections} tablosuna yansıtır ve
     * fatura hesabının adresini {@code addressId} ile bu projeksiyondan çözer.
     */
    public record AddressPayload(
            Long addressId,
            String city,
            String street,
            String houseNumber,
            String addressDescription,
            Boolean isPrimary
    ) {
    }
}
