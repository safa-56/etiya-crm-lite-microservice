package com.etiya.accountservice.business.messaging;

import com.etiya.accountservice.business.abstracts.BillingAccountSagaService;
import com.etiya.accountservice.business.abstracts.InboxService;
import com.etiya.accountservice.business.constants.BillingAccountSagaEvents;
import com.etiya.accountservice.business.dtos.events.BillingAccountSagaValidationPayload;
import com.etiya.accountservice.business.dtos.events.SagaEventEnvelope;
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
 * crm.BillingAccountSaga.events topic'ini dinleyen kafka tüketici binding'dir.
 * Bu topic'te hem account-service'in gönderdiği {CREATION_REQUESTED} hem de
 * customer-service'in ürettiği {@code CUSTOMER_VALIDATED}/{USTOMER_VALIDATION_FAILED}
 * olayları akar. Bu tüketici yalnızca sonuçları işler ve işlemeyi BillingAccountSagaManager'a devreder.
 */
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class BillingAccountSagaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(BillingAccountSagaConsumerConfig.class);

    private final InboxService inboxService;
    private final BillingAccountSagaService sagaService;
    private final ObjectMapper objectMapper;

    public BillingAccountSagaConsumerConfig(InboxService inboxService,
                                            BillingAccountSagaService sagaService,
                                            ObjectMapper objectMapper) {
        this.inboxService = inboxService;
        this.sagaService = sagaService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Consumer<Message<String>> billingAccountSagaConsumer() {
        return message -> {
            String payload = message.getPayload();
            String eventType = sniffEventType(payload);
            // Bu adım yalnızca doğrulama sonuçlarını işler; kendi gönderdiği istek
            // olayını (CREATION_REQUESTED) ve alakasız tipleri atlar.
            if (!BillingAccountSagaEvents.CUSTOMER_VALIDATED.equals(eventType)
                    && !BillingAccountSagaEvents.CUSTOMER_VALIDATION_FAILED.equals(eventType)) {
                return;
            }

            Object key = message.getHeaders().get(KafkaHeaders.RECEIVED_KEY);
            String messageId = "saga-result:" + (key != null ? key.toString() : "null")
                    + ":" + payload.hashCode();

            boolean processed = inboxService.process(messageId, eventType, () -> {
                BillingAccountSagaValidationPayload event = deserialize(payload);
                sagaService.applyValidationResult(event);
            });

            if (!processed) {
                log.debug("Duplicate consume atlandı. messageId={}", messageId);
            }
        };
    }

    /** Payload'ı önce zarfa ayrıştırıp olay tipini okur (somut tip seçimi için). */
    private String sniffEventType(String payload) {
        try {
            SagaEventEnvelope envelope = objectMapper.readValue(payload, SagaEventEnvelope.class);
            return envelope.eventType();
        } catch (Exception e) {
            log.warn("Saga olayı zarfı çözümlenemedi, atlanıyor: {}", payload);
            return null;
        }
    }

    private BillingAccountSagaValidationPayload deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, BillingAccountSagaValidationPayload.class);
        } catch (Exception e) {
            throw new IllegalStateException("Saga doğrulama sonucu JSON'dan çözümlenemedi: " + payload, e);
        }
    }
}
