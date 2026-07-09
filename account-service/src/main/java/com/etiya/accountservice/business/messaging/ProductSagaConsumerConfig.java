package com.etiya.accountservice.business.messaging;

import com.etiya.accountservice.business.abstracts.InboxService;
import com.etiya.accountservice.business.abstracts.ProductSagaParticipantService;
import com.etiya.accountservice.business.constants.ProductSagaEvents;
import com.etiya.accountservice.business.dtos.events.ProductSagaRequestedPayload;
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
 * Ürün satışı Saga'sının account-service (doğrulayıcı) tüketicisi —
 * {@code crm.ProductSaga.events}.
 *
 * <p>Bu topic'te hem istek ({@link ProductSagaEvents#SALE_REQUESTED}) hem de sonuç
 * olayları akar. Bu tüketici yalnızca istekleri işler; kendi ürettiği sonuç
 * olaylarını (validated/failed) payload'daki {@code eventType} ile ayırt edip atlar.
 * <b>Inbox Pattern</b> ile idempotenttir; yalnızca {@code app.kafka.enabled=true}
 * iken devreye girer. Binding adı {@code productSagaConsumer-in-0}.
 */
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class ProductSagaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(ProductSagaConsumerConfig.class);

    private final InboxService inboxService;
    private final ProductSagaParticipantService participantService;
    private final ObjectMapper objectMapper;

    public ProductSagaConsumerConfig(InboxService inboxService,
                                     ProductSagaParticipantService participantService,
                                     ObjectMapper objectMapper) {
        this.inboxService = inboxService;
        this.participantService = participantService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Consumer<Message<String>> productSagaConsumer() {
        return message -> {
            String payload = message.getPayload();
            ProductSagaRequestedPayload request = deserialize(payload);
            String eventType = request != null ? request.eventType() : null;
            // Yalnızca satış doğrulama İSTEKLERİ işlenir; kendi ürettiği sonuç
            // olayları ve çözümlenemeyen mesajlar atlanır.
            if (!ProductSagaEvents.SALE_REQUESTED.equals(eventType)) {
                return;
            }

            Object key = message.getHeaders().get(KafkaHeaders.RECEIVED_KEY);
            String messageId = "saga-request:" + (key != null ? key.toString() : "null")
                    + ":" + payload.hashCode();

            boolean processed = inboxService.process(
                    messageId, eventType,
                    () -> participantService.handleValidationRequest(request));

            if (!processed) {
                log.debug("Duplicate consume atlandı. messageId={}", messageId);
            }
        };
    }

    private ProductSagaRequestedPayload deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, ProductSagaRequestedPayload.class);
        } catch (Exception e) {
            log.warn("Ürün saga isteği JSON'dan çözümlenemedi, atlanıyor: {}", payload);
            return null;
        }
    }
}
