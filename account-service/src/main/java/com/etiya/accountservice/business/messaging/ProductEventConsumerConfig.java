package com.etiya.accountservice.business.messaging;

import com.etiya.accountservice.business.abstracts.InboxService;
import com.etiya.accountservice.business.abstracts.ProductProjectionService;
import com.etiya.accountservice.business.dtos.events.ProductEventPayload;
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
 * product-service'in ürün olaylarını tüketen binding (Spring Cloud Stream —
 * Kafka binder, fonksiyonel model).
 *
 * <p><b>Inbox Pattern</b> ile duplicate consume'a karşı korunur: Kafka en-az-bir-kez
 * teslim ettiğinden aynı olay tekrar gelebilir; {@link InboxService#process} ile
 * olay {@code messageId} bazında tekilleştirilir. Yeni olayda ürün projeksiyonu
 * ({@link ProductProjectionService}) güncellenir; bu, "aktif ürünü olan hesap
 * silinemez" kuralının veri kaynağıdır.
 *
 * <p>Binding adı {@code productEventConsumer-in-0}; topic ve group ayarları
 * {@code spring.cloud.stream.bindings.productEventConsumer-in-0.*} altında yapılır.
 * Yalnızca {@code app.kafka.enabled=true} iken devreye girer.
 */
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class ProductEventConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(ProductEventConsumerConfig.class);

    private final InboxService inboxService;
    private final ProductProjectionService productProjectionService;
    private final ObjectMapper objectMapper;

    public ProductEventConsumerConfig(InboxService inboxService,
                                      ProductProjectionService productProjectionService,
                                      ObjectMapper objectMapper) {
        this.inboxService = inboxService;
        this.productProjectionService = productProjectionService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Consumer<Message<String>> productEventConsumer() {
        return message -> {
            Object key = message.getHeaders().get(KafkaHeaders.RECEIVED_KEY);
            String payload = message.getPayload();
            String messageId = key != null ? key.toString() : String.valueOf(payload.hashCode());

            boolean processed = inboxService.process(messageId, "ProductEvent", () -> {
                ProductEventPayload event = deserialize(payload);
                productProjectionService.applyProductEvent(event);
            });

            if (!processed) {
                log.debug("Duplicate consume atlandı. messageId={}", messageId);
            }
        };
    }

    private ProductEventPayload deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, ProductEventPayload.class);
        } catch (Exception e) {
            // Ayrıştırılamayan olay iş akışını bozmalı; inbox transaction'ı geri alınır
            // ve olay yeniden teslimde tekrar denenebilir.
            throw new IllegalStateException("Ürün olayı JSON'dan çözümlenemedi: " + payload, e);
        }
    }
}
