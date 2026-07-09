package com.etiya.productservice.business.concretes;

import com.etiya.productservice.business.abstracts.OutboxService;
import com.etiya.productservice.dataAccess.OutboxEventRepository;
import com.etiya.productservice.entities.outbox.OutboxEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * {@link OutboxService} varsayılan uygulaması. Olayı JSON'a serialize edip
 * {@code outbox_events} tablosuna yazar; yayın işini Debezium (CDC) üstlenir.
 */
@Service
public class OutboxManager implements OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxManager(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(String aggregateType, String aggregateId, String eventType, Object payload) {
        OutboxEvent event = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(serialize(payload))
                .createdDate(LocalDateTime.now())
                .build();
        outboxEventRepository.save(event);
    }

    private String serialize(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            // Serialize edilemeyen bir payload iş akışını bozmalı (sessizce yutulmamalı).
            throw new IllegalStateException("Outbox payload JSON'a çevrilemedi", e);
        }
    }
}
