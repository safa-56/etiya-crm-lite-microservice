package com.etiya.accountservice.entities.enums;

/**
 * Fatura hesabı durumu.
 *
 * <p>Yeni oluşturulan hesap {@link #ACTIVE} atanır. Silme işleminde kayıt
 * fiziksel olarak silinmez; durum {@link #PASSIVE} yapılarak soft-delete uygulanır
 * (kabul kriteri).
 */
public enum AccountStatus {

    /** Aktif hesap. */
    ACTIVE,

    /** Pasifleştirilmiş (soft-delete edilmiş) hesap. */
    PASSIVE
}
