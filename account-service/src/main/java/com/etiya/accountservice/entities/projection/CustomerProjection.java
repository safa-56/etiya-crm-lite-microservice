package com.etiya.accountservice.entities.projection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * customer-service müşterisinin account-service'teki yerel <b>projeksiyonu</b> (read-model).
 *
 * <p>Kafka'dan tüketilen müşteri olaylarıyla güncel tutulur. Fatura hesabı
 * oluşturma/güncelleme kuralları "müşteri var mı?" kontrolünü bu tablo üzerinden
 * yapar; böylece customer-service'e senkron (REST) çağrı gerekmez ve servisler
 * gevşek bağlı kalır.
 *
 * <p>Kimlik ({@code customerId}) dışarıdan (customer-service) geldiğinden
 * üretilmez; olaydaki değer birebir kullanılır.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "customer_projections")
public class CustomerProjection {

    /** customer-service'teki müşteri id'si (üretilmez, olaydan gelir). */
    @Id
    @Column(name = "customer_id", nullable = false, updatable = false)
    private Long customerId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    /** Müşteri customer-service'te aktif mi (soft-delete yansıması). */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    /** Projeksiyonun son güncellenme zamanı (son işlenen olay). */
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
