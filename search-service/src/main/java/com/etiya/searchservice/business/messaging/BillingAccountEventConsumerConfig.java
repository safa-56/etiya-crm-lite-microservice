package com.etiya.searchservice.business.messaging;

import com.etiya.searchservice.business.abstracts.CustomerSearchIndexService;
import com.etiya.searchservice.business.abstracts.InboxService;
import com.etiya.searchservice.business.dtos.events.BillingAccountEventPayload;
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
 * account-service'in fatura hesabı olaylarını tüketen binding (Spring Cloud Stream —
 * Kafka binder, fonksiyonel model) — {@code crm.Account.events}.
 *
 * <p><b>Inbox Pattern</b> ile duplicate consume'a karşı korunur. Yeni olayda ilgili
 * müşteri satırına account/order numarası eklenir/çıkarılır (hesap durumuna göre;
 * bkz. {@link CustomerSearchIndexService#applyBillingAccountEvent}). Binding adı
 * {@code billingAccountEventConsumer-in-0}; yalnızca {@code app.kafka.enabled=true}
 * iken devreye girer.
 */
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class BillingAccountEventConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(BillingAccountEventConsumerConfig.class);

    private final InboxService inboxService;
    private final CustomerSearchIndexService indexService;
    private final ObjectMapper objectMapper;

    public BillingAccountEventConsumerConfig(InboxService inboxService,
                                             CustomerSearchIndexService indexService,
                                             ObjectMapper objectMapper) {
        this.inboxService = inboxService;
        this.indexService = indexService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Consumer<Message<String>> billingAccountEventConsumer() {
        return message -> {
            Object key = message.getHeaders().get(KafkaHeaders.RECEIVED_KEY);
            String payload = message.getPayload();
            String messageId = "account:" + (key != null ? key.toString() : "null")
                    + ":" + payload.hashCode();

            boolean processed = inboxService.process(messageId, "BillingAccountEvent", () -> {
                BillingAccountEventPayload event = deserialize(payload);
                indexService.applyBillingAccountEvent(event);
            });

            if (!processed) {
                log.debug("Duplicate consume atlandı. messageId={}", messageId);
            }
        };
    }

    private BillingAccountEventPayload deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, BillingAccountEventPayload.class);
        } catch (Exception e) {
            throw new IllegalStateException("Fatura hesabı olayı JSON'dan çözümlenemedi: " + payload, e);
        }
    }
}
