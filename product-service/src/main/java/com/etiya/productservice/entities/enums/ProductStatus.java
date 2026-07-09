package com.etiya.productservice.entities.enums;

/**
 * Ürün (satış) durumu.
 *
 * <p>Ürün satışı bir <b>choreography Saga</b> ile yürür: ürün önce {@link #PENDING}
 * olarak açılır; account-service fatura hesabını doğruladıktan sonra saga sonucuna
 * göre {@link #ACTIVE} (onay) ya da {@link #CANCELLED} (telafi) durumuna geçer.
 */
public enum ProductStatus {

    /** Saga başlatıldı; fatura hesabı doğrulaması bekleniyor (henüz aktif değil). */
    PENDING,

    /** Aktif ürün (saga başarıyla tamamlandı, satış kesinleşti). */
    ACTIVE,

    /** Saga telafisi: fatura hesabı doğrulanamadığı için iptal edilen ürün. */
    CANCELLED
}
