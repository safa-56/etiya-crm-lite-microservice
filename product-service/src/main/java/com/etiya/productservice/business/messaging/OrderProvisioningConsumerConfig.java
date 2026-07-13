package com.etiya.productservice.business.messaging;

import com.etiya.productservice.business.abstracts.InboxService;
import com.etiya.productservice.business.abstracts.ProductProvisioningService;
import com.etiya.productservice.business.constants.OrderProvisioningEvents;
import com.etiya.productservice.business.dtos.events.OrderConfirmedPayload;
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
 * order-service'in {@code crm.Order.events} kanalını dinleyen tüketici binding'i.
 *
 * <p>Bir sipariş CONFIRMED olduğunda order-service {@link OrderProvisioningEvents#ORDER_CONFIRMED}
 * olayını yayınlar; bu tüketici olayı {@link ProductProvisioningService}'e devrederek sipariş
 * kalemlerinden {@code Product} üretir (provizyon). İki servis birbirini doğrudan çağırmaz.
 * <b>Inbox Pattern</b> ile idempotenttir (aynı sipariş olayı tekrar gelse ürünler bir kez
 * üretilir); yalnızca {@code app.kafka.enabled=true} iken devreye girer.
 *
 * <p>Binding adı {@code orderProvisioningConsumer-in-0}.
 */
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class OrderProvisioningConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(OrderProvisioningConsumerConfig.class);

    private final InboxService inboxService;
    private final ProductProvisioningService provisioningService;
    private final ObjectMapper objectMapper;

    public OrderProvisioningConsumerConfig(InboxService inboxService,
                                           ProductProvisioningService provisioningService,
                                           ObjectMapper objectMapper) {
        this.inboxService = inboxService;
        this.provisioningService = provisioningService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Consumer<Message<String>> orderProvisioningConsumer() {
        return message -> {
            String payload = message.getPayload();
            OrderConfirmedPayload event = deserialize(payload);
            String eventType = event != null ? event.eventType() : null;
            // Yalnızca "sipariş kesinleşti" olayları işlenir; alakasız/çözümlenemeyen
            // mesajlar atlanır.
            if (!OrderProvisioningEvents.ORDER_CONFIRMED.equals(eventType)) {
                return;
            }

            Object key = message.getHeaders().get(KafkaHeaders.RECEIVED_KEY);
            String messageId = "order-provisioning:" + (key != null ? key.toString() : "null")
                    + ":" + payload.hashCode();

            boolean processed = inboxService.process(
                    messageId, eventType,
                    () -> provisioningService.provisionFromOrder(event));

            if (!processed) {
                log.debug("Duplicate consume atlandı. messageId={}", messageId);
            }
        };
    }

    private OrderConfirmedPayload deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, OrderConfirmedPayload.class);
        } catch (Exception e) {
            log.warn("Sipariş provizyon olayı JSON'dan çözümlenemedi, atlanıyor: {}", payload);
            return null;
        }
    }
}
