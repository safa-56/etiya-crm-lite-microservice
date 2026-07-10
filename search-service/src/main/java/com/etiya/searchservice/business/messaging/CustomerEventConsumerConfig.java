package com.etiya.searchservice.business.messaging;

import com.etiya.searchservice.business.abstracts.CustomerSearchIndexService;
import com.etiya.searchservice.business.abstracts.InboxService;
import com.etiya.searchservice.business.dtos.events.CustomerEventPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

/**
 * customer-service'in müşteri olaylarını tüketen binding (Spring Cloud Stream —
 * Kafka binder, fonksiyonel model) — {@code crm.Customer.events}.
 *
 * <p><b>Inbox Pattern</b> ile duplicate consume'a karşı korunur. Yeni olayda arama
 * indeksi ({@link CustomerSearchIndexService}) create/update için upsert edilir,
 * delete için kaldırılır. Binding adı {@code customerEventConsumer-in-0}; yalnızca
 * {@code app.kafka.enabled=true} iken devreye girer (test profilinde false).
 */
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class CustomerEventConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(CustomerEventConsumerConfig.class);

    private final InboxService inboxService;
    private final CustomerSearchIndexService indexService;
    private final ObjectMapper objectMapper;

    public CustomerEventConsumerConfig(InboxService inboxService,
                                       CustomerSearchIndexService indexService,
                                       ObjectMapper objectMapper) {
        this.inboxService = inboxService;
        this.indexService = indexService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Consumer<Message<String>> customerEventConsumer() {
        return message -> {
            Object key = message.getHeaders().get(KafkaHeaders.RECEIVED_KEY);
            String payload = message.getPayload();
            // Aynı agregada ardışık farklı olayların ayrı işlenmesi için anahtar + hash.
            String messageId = "customer:" + (key != null ? key.toString() : "null")
                    + ":" + payload.hashCode();

            boolean processed = inboxService.process(messageId, "CustomerEvent", () -> {
                CustomerEventPayload event = deserialize(payload);
                indexService.applyCustomerEvent(event);
            });

            if (!processed) {
                log.debug("Duplicate consume atlandı. messageId={}", messageId);
            }
        };
    }

    private CustomerEventPayload deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, CustomerEventPayload.class);
        } catch (Exception e) {
            // Ayrıştırılamayan olay iş akışını bozmalı; inbox transaction'ı geri alınır
            // ve olay yeniden teslimde tekrar denenebilir.
            throw new IllegalStateException("Müşteri olayı JSON'dan çözümlenemedi: " + payload, e);
        }
    }
}
