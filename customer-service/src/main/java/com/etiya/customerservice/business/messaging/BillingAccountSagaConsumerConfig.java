package com.etiya.customerservice.business.messaging;

import com.etiya.customerservice.business.abstracts.BillingAccountSagaParticipantService;
import com.etiya.customerservice.business.abstracts.InboxService;
import com.etiya.customerservice.business.constants.BillingAccountSagaEvents;
import com.etiya.customerservice.business.dtos.events.BillingAccountSagaRequestedPayload;
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
 * Fatura hesabı Saga'sının customer-service (doğrulayıcı) tüketicisi —
 * {@code crm.BillingAccountSaga.events}.
 *
 * <p>Bu topic'te hem istek ({@code CREATION_REQUESTED}) hem de sonuç olayları akar.
 * Bu tüketici yalnızca <b>istekleri</b> işler; kendi ürettiği sonuç olaylarını
 * (validated/failed) payload'daki {@code eventType} ile ayırt edip atlar.
 *
 * <p><b>Inbox Pattern</b> ile idempotent; yalnızca {@code app.kafka.enabled=true}
 * iken devreye girer. Binding adı {@code billingAccountSagaConsumer-in-0}.
 */
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class BillingAccountSagaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(BillingAccountSagaConsumerConfig.class);

    private final InboxService inboxService;
    private final BillingAccountSagaParticipantService participantService;
    private final ObjectMapper objectMapper;

    public BillingAccountSagaConsumerConfig(InboxService inboxService,
                                            BillingAccountSagaParticipantService participantService,
                                            ObjectMapper objectMapper) {
        this.inboxService = inboxService;
        this.participantService = participantService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Consumer<Message<String>> billingAccountSagaConsumer() {
        return message -> {
            String payload = message.getPayload();
            BillingAccountSagaRequestedPayload request = deserialize(payload);
            if (request == null
                    || !BillingAccountSagaEvents.CREATION_REQUESTED.equals(request.eventType())) {
                // Sonuç olayı ya da çözümlenemeyen mesaj: bu adım işlemez.
                return;
            }

            Object key = message.getHeaders().get(KafkaHeaders.RECEIVED_KEY);
            String messageId = "saga-request:" + (key != null ? key.toString() : "null")
                    + ":" + payload.hashCode();

            boolean processed = inboxService.process(
                    messageId, BillingAccountSagaEvents.CREATION_REQUESTED,
                    () -> participantService.handleCreationRequested(request));

            if (!processed) {
                log.debug("Duplicate consume atlandı. messageId={}", messageId);
            }
        };
    }

    private BillingAccountSagaRequestedPayload deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, BillingAccountSagaRequestedPayload.class);
        } catch (Exception e) {
            log.warn("Saga isteği JSON'dan çözümlenemedi, atlanıyor: {}", payload);
            return null;
        }
    }
}
