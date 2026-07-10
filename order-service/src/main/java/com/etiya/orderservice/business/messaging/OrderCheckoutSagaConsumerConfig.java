package com.etiya.orderservice.business.messaging;

import com.etiya.orderservice.business.abstracts.InboxService;
import com.etiya.orderservice.business.abstracts.OrderSagaService;
import com.etiya.orderservice.business.constants.OrderCheckoutSagaEvents;
import com.etiya.orderservice.business.dtos.events.OrderCheckoutValidationPayload;
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
 * Sepetten siparişe geçiş Saga'sının order-service (başlatıcı) tüketicisi —
 * {@code crm.OrderCheckoutSaga.events}.
 *
 * <p>Bu topic'te hem order-service'in gönderdiği istek
 * ({@link OrderCheckoutSagaEvents#CHECKOUT_REQUESTED}) hem de cart-service'in ürettiği
 * sonuç olayları akar. Bu tüketici yalnızca doğrulama SONUÇLARINI işler ve
 * {@link OrderSagaService}'e devreder; kendi gönderdiği istek olayını atlar (döngü yok).
 * <b>Inbox Pattern</b> ile idempotenttir; yalnızca {@code app.kafka.enabled=true} iken
 * devreye girer. Binding adı {@code orderCheckoutConsumer-in-0}.
 */
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class OrderCheckoutSagaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(OrderCheckoutSagaConsumerConfig.class);

    private final InboxService inboxService;
    private final OrderSagaService sagaService;
    private final ObjectMapper objectMapper;

    public OrderCheckoutSagaConsumerConfig(InboxService inboxService,
                                           OrderSagaService sagaService,
                                           ObjectMapper objectMapper) {
        this.inboxService = inboxService;
        this.sagaService = sagaService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Consumer<Message<String>> orderCheckoutConsumer() {
        return message -> {
            String payload = message.getPayload();
            OrderCheckoutValidationPayload result = deserialize(payload);
            String eventType = result != null ? result.eventType() : null;
            // Yalnızca doğrulama sonuçları işlenir; kendi gönderdiği istek olayı ve
            // çözümlenemeyen mesajlar atlanır.
            if (!OrderCheckoutSagaEvents.CART_VALIDATED.equals(eventType)
                    && !OrderCheckoutSagaEvents.CART_VALIDATION_FAILED.equals(eventType)) {
                return;
            }

            Object key = message.getHeaders().get(KafkaHeaders.RECEIVED_KEY);
            String messageId = "order-saga-result:" + (key != null ? key.toString() : "null")
                    + ":" + payload.hashCode();

            boolean processed = inboxService.process(
                    messageId, eventType,
                    () -> sagaService.applyValidationResult(result));

            if (!processed) {
                log.debug("Duplicate consume atlandı. messageId={}", messageId);
            }
        };
    }

    private OrderCheckoutValidationPayload deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, OrderCheckoutValidationPayload.class);
        } catch (Exception e) {
            log.warn("Sipariş saga sonucu JSON'dan çözümlenemedi, atlanıyor: {}", payload);
            return null;
        }
    }
}
