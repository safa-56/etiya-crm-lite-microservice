package com.etiya.accountservice.entities.enums;

/**
 * Fatura hesabı durumu.
 *
 * <p>Fatura hesabı oluşturma bir <b>choreography Saga</b> ile yürür: hesap önce
 * {@link #PENDING} olarak açılır; customer-service müşteri/adres doğrulamasını
 * yaptıktan sonra saga sonucuna göre {@link #ACTIVE} (onay) ya da {@link #CANCELLED}
 * (telafi) durumuna geçer. Silme işleminde kayıt fiziksel olarak silinmez; durum
 * {@link #PASSIVE} yapılarak soft-delete uygulanır (kabul kriteri).
 */
public enum AccountStatus {

    /** Saga başlatıldı; müşteri/adres doğrulaması bekleniyor (henüz kullanılamaz). */
    PENDING,

    /** Aktif hesap (saga başarıyla tamamlandı). */
    ACTIVE,

    /** Saga telafisi: doğrulama başarısız olduğu için iptal edilen hesap. */
    CANCELLED,

    /** Pasifleştirilmiş (soft-delete edilmiş) hesap. */
    PASSIVE
}
