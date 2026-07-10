package com.etiya.orderservice.entities.enums;

/**
 * Siparişin Saga durumu.
 *
 * <p>Sepetten siparişe geçiş (checkout), cart-service ile yürüyen bir
 * <b>choreography Saga</b> ile kesinleşir (account ↔ customer / product ↔ account /
 * cart ↔ product modeliyle aynı):
 * <ul>
 *   <li>{@link #PENDING}   : sipariş oluşturuldu, sepet doğrulaması bekleniyor
 *       (satırlar/toplam henüz snapshot'lanmadı).</li>
 *   <li>{@link #CONFIRMED} : cart-service sepeti doğruladı; satır ve toplam tutar
 *       snapshot'ı yazıldı, sipariş kesinleşti.</li>
 *   <li>{@link #CANCELLED} : doğrulama başarısız (sepet yok/boş); telafi
 *       (compensation) ile sipariş pasifleştirildi (soft-delete).</li>
 * </ul>
 */
public enum OrderStatus {

    /** Doğrulama bekleniyor (saga adım 1 gönderildi). */
    PENDING,

    /** Doğrulandı; sipariş kesinleşti (satırlar + toplam yazıldı). */
    CONFIRMED,

    /** Doğrulanamadı; telafi ile iptal edildi. */
    CANCELLED
}
