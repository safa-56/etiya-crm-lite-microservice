package com.etiya.productservice.business.messaging;

import com.etiya.productservice.business.abstracts.InboxService;
import com.etiya.productservice.business.abstracts.ProductSagaService;
import com.etiya.productservice.business.constants.ProductSagaEvents;
import com.etiya.productservice.business.dtos.events.ProductSagaValidationPayload;
import com.etiya.productservice.business.dtos.events.SagaEventEnvelope;
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
 * {@code crm.ProductSaga.events} topic'ini dinleyen Kafka tüketici binding'i
 * (product-service — başlatıcı taraf).
 *
 * <p>Bu topic'te hem product-service'in gönderdiği {@link ProductSagaEvents#SALE_REQUESTED}
 * hem de account-service'in ürettiği {@link ProductSagaEvents#ACCOUNT_VALIDATED}/
 * {@link ProductSagaEvents#ACCOUNT_VALIDATION_FAILED} olayları akar. Bu tüketici
 * yalnızca doğrulama SONUÇLARINI işler ve {@link ProductSagaService}'e devreder;
 * kendi gönderdiği istek olayını atlar. <b>Inbox Pattern</b> ile idempotenttir.
 *
 * <p>Binding adı {@code productSagaConsumer-in-0}; yalnızca {@code app.kafka.enabled=true}
 * iken devreye girer.
 */
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class ProductSagaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(ProductSagaConsumerConfig.class);

    private final InboxService inboxService;
    private final ProductSagaService sagaService;
    private final ObjectMapper objectMapper;

    public ProductSagaConsumerConfig(InboxService inboxService,
                                     ProductSagaService sagaService,
                                     ObjectMapper objectMapper) {
        this.inboxService = inboxService;
        this.sagaService = sagaService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Consumer<Message<String>> productSagaConsumer() {
        return message -> {
            String payload = message.getPayload();
            String eventType = sniffEventType(payload);
            // Bu adım yalnızca doğrulama sonuçlarını işler; kendi gönderdiği istek
            // olayını (SALE_REQUESTED) ve alakasız tipleri atlar.
            if (!ProductSagaEvents.ACCOUNT_VALIDATED.equals(eventType)
                    && !ProductSagaEvents.ACCOUNT_VALIDATION_FAILED.equals(eventType)) {
                return;
            }

            Object key = message.getHeaders().get(KafkaHeaders.RECEIVED_KEY);
            String messageId = "saga-result:" + (key != null ? key.toString() : "null")
                    + ":" + payload.hashCode();

            boolean processed = inboxService.process(messageId, eventType, () -> {
                ProductSagaValidationPayload event = deserialize(payload);
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

    private ProductSagaValidationPayload deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, ProductSagaValidationPayload.class);
        } catch (Exception e) {
            throw new IllegalStateException("Saga doğrulama sonucu JSON'dan çözümlenemedi: " + payload, e);
        }
    }
}
