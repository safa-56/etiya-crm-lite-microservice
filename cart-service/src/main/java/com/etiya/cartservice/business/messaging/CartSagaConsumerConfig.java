package com.etiya.cartservice.business.messaging;

import com.etiya.cartservice.business.abstracts.CartSagaService;
import com.etiya.cartservice.business.abstracts.InboxService;
import com.etiya.cartservice.business.constants.CartSagaEvents;
import com.etiya.cartservice.business.dtos.events.CartItemSagaValidationPayload;
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
 * Sepete ekleme Saga'sının cart-service (başlatıcı) tüketicisi —
 * {@code crm.CartSaga.events}.
 *
 * <p>Bu topic'te hem cart-service'in gönderdiği istek
 * ({@link CartSagaEvents#ITEM_VALIDATION_REQUESTED}) hem de product-service'in ürettiği
 * sonuç olayları akar. Bu tüketici yalnızca doğrulama SONUÇLARINI işler ve
 * {@link CartSagaService}'e devreder; kendi gönderdiği istek olayını atlar (döngü yok).
 * <b>Inbox Pattern</b> ile idempotenttir; yalnızca {@code app.kafka.enabled=true} iken
 * devreye girer. Binding adı {@code cartSagaConsumer-in-0}.
 */
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class CartSagaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(CartSagaConsumerConfig.class);

    private final InboxService inboxService;
    private final CartSagaService sagaService;
    private final ObjectMapper objectMapper;

    public CartSagaConsumerConfig(InboxService inboxService,
                                  CartSagaService sagaService,
                                  ObjectMapper objectMapper) {
        this.inboxService = inboxService;
        this.sagaService = sagaService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Consumer<Message<String>> cartSagaConsumer() {
        return message -> {
            String payload = message.getPayload();
            CartItemSagaValidationPayload result = deserialize(payload);
            String eventType = result != null ? result.eventType() : null;
            // Yalnızca doğrulama sonuçları işlenir; kendi gönderdiği istek olayı ve
            // çözümlenemeyen mesajlar atlanır.
            if (!CartSagaEvents.ITEM_VALIDATED.equals(eventType)
                    && !CartSagaEvents.ITEM_VALIDATION_FAILED.equals(eventType)) {
                return;
            }

            Object key = message.getHeaders().get(KafkaHeaders.RECEIVED_KEY);
            String messageId = "cart-saga-result:" + (key != null ? key.toString() : "null")
                    + ":" + payload.hashCode();

            boolean processed = inboxService.process(
                    messageId, eventType,
                    () -> sagaService.applyValidationResult(result));

            if (!processed) {
                log.debug("Duplicate consume atlandı. messageId={}", messageId);
            }
        };
    }

    private CartItemSagaValidationPayload deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, CartItemSagaValidationPayload.class);
        } catch (Exception e) {
            log.warn("Sepet saga sonucu JSON'dan çözümlenemedi, atlanıyor: {}", payload);
            return null;
        }
    }
}
