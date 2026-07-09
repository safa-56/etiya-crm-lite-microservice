package com.etiya.productservice.entities.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transactional Outbox kaydı.
 *
 * <p>İş verisiyle <b>aynı transaction</b> içinde bu tabloya yazılır; böylece
 * "DB'ye yaz ama Kafka'ya yollayamadan çök" (ghost event) durumu ortadan kalkar.
 * Debezium bu tabloyu (CDC) izler ve {@code EventRouter} SMT ile olayı
 * {@code crm.<aggregateType>.events} topic'ine yönlendirir.
 *
 * <p>Alan adları, {@code infra/debezium/register-product-connector.json}
 * içindeki EventRouter eşlemeleriyle birebir uyumludur.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** Agrega tipi (route.by.field) — ör. "Product". Topic yönlendirmesini belirler. */
    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    /** Agrega kimliği (event.key) — ör. ürün id'si. Kafka mesaj anahtarı olur. */
    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    /** Olay tipi (event.type) — ör. "ProductCreated", "ProductDeleted". */
    @Column(name = "event_type", nullable = false)
    private String eventType;

    /** Olay gövdesi (event.payload) — JSON string. */
    @Column(name = "payload", columnDefinition = "text")
    private String payload;

    /** Oluşturulma zamanı (event.timestamp). */
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;
}
