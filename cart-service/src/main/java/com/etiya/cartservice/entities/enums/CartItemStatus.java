package com.etiya.cartservice.entities.enums;

/**
 * Sepet satırının Saga durumu.
 *
 * <p>Sepete ekleme, product-service ile yürüyen bir <b>choreography Saga</b> ile
 * kesinleşir (account ↔ customer / product ↔ account modeliyle aynı):
 * <ul>
 *   <li>{@link #PENDING}   : satır oluşturuldu, teklif/kampanya doğrulaması bekleniyor.</li>
 *   <li>{@link #ACTIVE}    : product-service doğruladı; ad/fiyat snapshot'ı yazıldı,
 *       satır sepet toplamına dahildir.</li>
 *   <li>{@link #CANCELLED} : doğrulama başarısız; telafi (compensation) ile satır
 *       pasifleştirildi (soft-delete), toplamdan düşer.</li>
 * </ul>
 */
public enum CartItemStatus {

    /** Doğrulama bekleniyor (saga adım 1 gönderildi). */
    PENDING,

    /** Doğrulandı; satır aktif ve fiyatlı. */
    ACTIVE,

    /** Doğrulanamadı; telafi ile iptal edildi. */
    CANCELLED
}
