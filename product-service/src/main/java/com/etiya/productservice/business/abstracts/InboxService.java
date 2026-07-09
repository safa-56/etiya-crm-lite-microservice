package com.etiya.productservice.business.abstracts;

/**
 * Inbox Pattern servisi — tüketilen olayların idempotent işlenmesini sağlar
 * (duplicate consume önleme).
 */
public interface InboxService {

    /**
     * Olayı tekilleştirerek işler. Aynı {@code messageId} daha önce işlendiyse
     * {@code handler} çalıştırılmaz.
     *
     * @param messageId olayın benzersiz kimliği
     * @param eventType olay tipi (kayıt/log için)
     * @param handler   yalnızca ilk (yeni) olayda çalışacak iş mantığı
     * @return olay bu çağrıda işlendiyse {@code true}, daha önce işlendiği için
     *         atlandıysa {@code false}
     */
    boolean process(String messageId, String eventType, Runnable handler);
}
