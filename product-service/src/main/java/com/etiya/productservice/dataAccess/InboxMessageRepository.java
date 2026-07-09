package com.etiya.productservice.dataAccess;

import com.etiya.productservice.entities.inbox.InboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Inbox kayıtları için veri erişimi (idempotent tüketim / duplicate consume önleme).
 */
@Repository
public interface InboxMessageRepository extends JpaRepository<InboxMessage, String> {

    /** Bu {@code messageId}'ye sahip bir olay daha önce işlendi mi? */
    boolean existsByMessageId(String messageId);
}
