package com.etiya.customerservice.business.messaging;

import com.etiya.customerservice.business.abstracts.InboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

/**
 * Kafka'dan gelen entegrasyon olaylarını tüketen <b>örnek</b> binding
 * (Spring Cloud Stream — Kafka binder, fonksiyonel model).
 *
 * <p>Amaç: <b>Inbox Pattern</b>'in duplicate consume'a karşı nasıl kullanıldığını
 * göstermek. Kafka en-az-bir-kez teslim ettiğinden aynı olay tekrar gelebilir;
 * {@link InboxService#process} ile olay {@code messageId} bazında tekilleştirilir.
 *
 * <p>Binding adı {@code inboundEventConsumer-in-0}; topic ve group ayarları
 * {@code spring.cloud.stream.bindings.inboundEventConsumer-in-0.*} altında yapılır.
 * Yalnızca {@code app.kafka.enabled=true} iken devreye girer (test/dev'de kapatılabilir).
 */
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class InboundEventConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(InboundEventConsumerConfig.class);

    private final InboxService inboxService;

    public InboundEventConsumerConfig(InboxService inboxService) {
        this.inboxService = inboxService;
    }

    @Bean
    public Consumer<Message<String>> inboundEventConsumer() {
        return message -> {
            // Outbox/Debezium olaylarında mesaj anahtarı (aggregate_id) tekilleştirme
            // için uygundur; anahtar yoksa payload hash'i gibi bir alternatif kullanılır.
            Object key = message.getHeaders().get(KafkaHeaders.RECEIVED_KEY);
            String payload = message.getPayload();
            String messageId = key != null ? key.toString() : String.valueOf(payload.hashCode());

            boolean processed = inboxService.process(messageId, "InboundEvent", () -> {
                // Gerçek iş mantığı burada çalışır (yalnızca ilk teslimde).
                log.info("Inbound olay işleniyor. messageId={}, payload={}", messageId, payload);
            });

            if (!processed) {
                log.debug("Duplicate consume atlandı. messageId={}", messageId);
            }
        };
    }
}
