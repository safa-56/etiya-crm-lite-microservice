package com.etiya.customerservice.business.concretes;

import com.etiya.customerservice.business.abstracts.InboxService;
import com.etiya.customerservice.dataAccess.InboxMessageRepository;
import com.etiya.customerservice.entities.inbox.InboxMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * {@link InboxService} varsayılan uygulaması.
 *
 * <p>Olay işlenmeden önce {@code messageId} inbox'a yazılır; kayıt zaten varsa
 * olay daha önce işlenmiştir ve atlanır. İşleme ve inbox yazımı aynı transaction
 * içinde olduğundan, iş mantığı başarısız olursa inbox kaydı da geri alınır
 * (böylece yeniden teslimde tekrar denenebilir).
 */
@Service
public class InboxManager implements InboxService {

    private final InboxMessageRepository inboxMessageRepository;

    public InboxManager(InboxMessageRepository inboxMessageRepository) {
        this.inboxMessageRepository = inboxMessageRepository;
    }

    @Override
    @Transactional
    public boolean process(String messageId, String eventType, Runnable handler) {
        if (inboxMessageRepository.existsByMessageId(messageId)) {
            // Duplicate consume: bu olay zaten işlenmiş, tekrar işleme.
            return false;
        }

        inboxMessageRepository.save(InboxMessage.builder()
                .messageId(messageId)
                .eventType(eventType)
                .receivedDate(LocalDateTime.now())
                .build());

        handler.run();
        return true;
    }
}
