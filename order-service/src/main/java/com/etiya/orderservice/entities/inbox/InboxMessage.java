package com.etiya.orderservice.entities.inbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Inbox kaydı (Inbox Pattern).
 *
 * <p>Kafka'dan gelen olaylar en-az-bir-kez (at-least-once) teslim edildiğinden
 * aynı olay birden çok kez tüketilebilir (<b>duplicate consume</b>). Tüketici,
 * işlemeden önce olayın {@code messageId}'sini bu tabloya yazmayı dener; kayıt
 * zaten varsa olay daha önce işlenmiş demektir ve atlanır (idempotency).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "inbox_messages")
public class InboxMessage {

    /** Olayın benzersiz kimliği (Kafka mesaj id'si / event id). Tekilleştirme anahtarı. */
    @Id
    @Column(name = "message_id", nullable = false, updatable = false)
    private String messageId;

    @Column(name = "event_type")
    private String eventType;

    /** Olayın işlendiği (tüketildiği) zaman. */
    @Column(name = "received_date", nullable = false)
    private LocalDateTime receivedDate;
}
