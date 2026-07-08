package com.etiya.accountservice.business.messaging;

import com.etiya.accountservice.business.abstracts.CustomerProjectionService;
import com.etiya.accountservice.business.abstracts.InboxService;
import com.etiya.accountservice.business.dtos.events.CustomerEventPayload;
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
 * Kafka binder, fonksiyonel model).
 *
 * <p>Bu tüketici, account-service'in yerel müşteri projeksiyonunu (read-model)
 * güncel tutar; fatura hesabı CRUD'unda "müşteri var mı?" ve "adres bu müşteriye
 * ait mi?" tutarlılık kuralları bu projeksiyondan beslenir. Böylece account-service,
 * customer-service'e senkron REST çağrısı yapmadan gerekli müşteri/adres verisine
 * erişir (gevşek bağlılık).
 *
 * <p><b>Inbox Pattern</b> ile duplicate consume'a karşı korunur: aynı olay tekrar
 * gelirse {@link InboxService#process} ile atlanır. Binding adı
 * {@code customerEventConsumer-in-0}; topic/group ayarları
 * {@code spring.cloud.stream.bindings.customerEventConsumer-in-0.*} altındadır.
 * Yalnızca {@code app.kafka.enabled=true} iken devreye girer.
 */
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class CustomerEventConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(CustomerEventConsumerConfig.class);

    private final InboxService inboxService;
    private final CustomerProjectionService customerProjectionService;
    private final ObjectMapper objectMapper;

    public CustomerEventConsumerConfig(InboxService inboxService,
                                       CustomerProjectionService customerProjectionService,
                                       ObjectMapper objectMapper) {
        this.inboxService = inboxService;
        this.customerProjectionService = customerProjectionService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Consumer<Message<String>> customerEventConsumer() {
        return message -> {
            Object key = message.getHeaders().get(KafkaHeaders.RECEIVED_KEY);
            String payload = message.getPayload();
            // Aynı müşteri için olaylar aynı anahtarla (customerId) gelir; sıra korunur.
            // messageId olarak "key + payload hash" kullanılır: aynı müşterinin farklı
            // olayları tekilleşmeden işlensin, tekrar teslimler ise atlansın.
            String messageId = (key != null ? key.toString() : "null") + ":" + payload.hashCode();

            boolean processed = inboxService.process(messageId, "CustomerEvent", () -> {
                CustomerEventPayload event = deserialize(payload);
                customerProjectionService.applyCustomerEvent(event);
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
