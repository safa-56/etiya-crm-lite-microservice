package com.etiya.productservice.business.messaging;

import com.etiya.productservice.business.abstracts.CartSagaParticipantService;
import com.etiya.productservice.business.abstracts.InboxService;
import com.etiya.productservice.business.constants.CartSagaEvents;
import com.etiya.productservice.business.dtos.events.CartSagaRequestedPayload;
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
 * Sepet ekleme Saga'sının product-service (doğrulayıcı) tüketicisi —
 * {@code crm.CartSaga.events}.
 *
 * <p>Bu topic'te hem cart-service'in gönderdiği istek
 * ({@link CartSagaEvents#ITEM_VALIDATION_REQUESTED}) hem de product-service'in ürettiği
 * sonuç olayları akar. Bu tüketici yalnızca İSTEKLERİ işler ve
 * {@link CartSagaParticipantService}'e devreder; kendi ürettiği sonuç olaylarını
 * (validated/failed) payload'daki {@code eventType} ile ayırt edip atlar (döngü yok).
 * <b>Inbox Pattern</b> ile idempotenttir; yalnızca {@code app.kafka.enabled=true} iken
 * devreye girer. Binding adı {@code cartSagaConsumer-in-0}.
 */
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class CartSagaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(CartSagaConsumerConfig.class);

    private final InboxService inboxService;
    private final CartSagaParticipantService participantService;
    private final ObjectMapper objectMapper;

    public CartSagaConsumerConfig(InboxService inboxService,
                                  CartSagaParticipantService participantService,
                                  ObjectMapper objectMapper) {
        this.inboxService = inboxService;
        this.participantService = participantService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Consumer<Message<String>> cartSagaConsumer() {
        return message -> {
            String payload = message.getPayload();
            CartSagaRequestedPayload request = deserialize(payload);
            String eventType = request != null ? request.eventType() : null;
            // Yalnızca doğrulama İSTEKLERİ işlenir; kendi ürettiği sonuç olayları ve
            // çözümlenemeyen mesajlar atlanır.
            if (!CartSagaEvents.ITEM_VALIDATION_REQUESTED.equals(eventType)) {
                return;
            }

            Object key = message.getHeaders().get(KafkaHeaders.RECEIVED_KEY);
            String messageId = "cart-saga-request:" + (key != null ? key.toString() : "null")
                    + ":" + payload.hashCode();

            boolean processed = inboxService.process(
                    messageId, eventType,
                    () -> participantService.handleValidationRequest(request));

            if (!processed) {
                log.debug("Duplicate consume atlandı. messageId={}", messageId);
            }
        };
    }

    private CartSagaRequestedPayload deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, CartSagaRequestedPayload.class);
        } catch (Exception e) {
            log.warn("Sepet saga isteği JSON'dan çözümlenemedi, atlanıyor: {}", payload);
            return null;
        }
    }
}
