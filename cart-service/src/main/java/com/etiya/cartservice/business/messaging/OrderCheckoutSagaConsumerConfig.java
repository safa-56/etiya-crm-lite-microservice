package com.etiya.cartservice.business.messaging;

import com.etiya.cartservice.business.abstracts.InboxService;
import com.etiya.cartservice.business.abstracts.OrderCheckoutParticipantService;
import com.etiya.cartservice.business.constants.OrderCheckoutSagaEvents;
import com.etiya.cartservice.business.dtos.events.OrderCheckoutRequestedPayload;
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
 * Sepetten siparişe geçiş Saga'sının cart-service (doğrulayıcı) tüketicisi —
 * {@code crm.OrderCheckoutSaga.events}.
 *
 * <p>Bu topic'te hem order-service'in gönderdiği istek
 * ({@link OrderCheckoutSagaEvents#CHECKOUT_REQUESTED}) hem de cart-service'in kendi ürettiği
 * sonuç olayları akar. Bu tüketici yalnızca doğrulama İSTEKLERİNİ işler ve
 * {@link OrderCheckoutParticipantService}'e devreder; kendi ürettiği sonuç olaylarını atlar
 * (döngü yok). <b>Inbox Pattern</b> ile idempotenttir; yalnızca {@code app.kafka.enabled=true}
 * iken devreye girer. Binding adı {@code orderCheckoutRequestConsumer-in-0}.
 */
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class OrderCheckoutSagaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(OrderCheckoutSagaConsumerConfig.class);

    private final InboxService inboxService;
    private final OrderCheckoutParticipantService participantService;
    private final ObjectMapper objectMapper;

    public OrderCheckoutSagaConsumerConfig(InboxService inboxService,
                                           OrderCheckoutParticipantService participantService,
                                           ObjectMapper objectMapper) {
        this.inboxService = inboxService;
        this.participantService = participantService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Consumer<Message<String>> orderCheckoutRequestConsumer() {
        return message -> {
            String payload = message.getPayload();
            OrderCheckoutRequestedPayload request = deserialize(payload);
            String eventType = request != null ? request.eventType() : null;
            // Yalnızca doğrulama istekleri işlenir; kendi ürettiği sonuç olayları ve
            // çözümlenemeyen mesajlar atlanır.
            if (!OrderCheckoutSagaEvents.CHECKOUT_REQUESTED.equals(eventType)) {
                return;
            }

            Object key = message.getHeaders().get(KafkaHeaders.RECEIVED_KEY);
            String messageId = "order-saga-request:" + (key != null ? key.toString() : "null")
                    + ":" + payload.hashCode();

            boolean processed = inboxService.process(
                    messageId, eventType,
                    () -> participantService.handleValidationRequest(request));

            if (!processed) {
                log.debug("Duplicate consume atlandı. messageId={}", messageId);
            }
        };
    }

    private OrderCheckoutRequestedPayload deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, OrderCheckoutRequestedPayload.class);
        } catch (Exception e) {
            log.warn("Sipariş saga isteği JSON'dan çözümlenemedi, atlanıyor: {}", payload);
            return null;
        }
    }
}
