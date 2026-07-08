package com.etiya.accountservice.business.dtos.events;

import java.time.LocalDateTime;
import java.util.List;

/**
 * customer-service'ten ({@code crm.Customer.events}) tüketilen müşteri olayı gövdesi.
 *
 * <p>account-service, fatura hesabı CRUD'unda tutarlılık için müşteri kimliğine
 * ve müşterinin adreslerine ihtiyaç duyar. Bu olay tüketildiğinde yerel
 * <b>müşteri projeksiyonu</b> ({@code customer_projections} +
 * {@code customer_address_projections}) güncellenir; fatura hesabı oluşturma/güncelleme
 * kuralları bu projeksiyona bakar (customer-service'e senkron çağrı yapılmaz).
 *
 * <p>Alanlar, customer-service tarafındaki üretici sözleşmesiyle birebir eşleşir.
 *
 * @param customerId müşteri kimliği (customer-service'teki id)
 * @param firstName  ad (standalone adres olaylarında {@code null} gelebilir)
 * @param lastName   soyad (standalone adres olaylarında {@code null} gelebilir)
 * @param eventType  "CustomerCreated" / "CustomerUpdated" / "CustomerDeleted"
 * @param addresses  müşterinin o anki aktif adres kümesi
 * @param occurredAt olayın üretildiği zaman
 */
public record CustomerEventPayload(
        Long customerId,
        String firstName,
        String lastName,
        String eventType,
        List<AddressPayload> addresses,
        LocalDateTime occurredAt
) {

    /** Müşteriye ait tek bir adresin olay gövdesindeki gösterimi. */
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
