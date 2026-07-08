package com.etiya.accountservice.dataAccess;

import com.etiya.accountservice.entities.outbox.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Transactional Outbox kayıtları için veri erişimi. Yazma, iş verisiyle aynı
 * transaction içinde yapılır; okuma/yayın işini Debezium (CDC) üstlenir.
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
}
