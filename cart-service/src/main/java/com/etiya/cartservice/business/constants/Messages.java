package com.etiya.cartservice.business.constants;

/**
 * İş katmanı mesaj sabitleri.
 *
 * <p>Kullanıcıya/istemciye dönen tüm iş mesajları magic string olarak değil,
 * buradaki sabitler üzerinden verilir. Böylece mesajlar tek yerden yönetilir
 * ve ileride i18n'e taşınması kolaylaşır.
 */
public final class Messages {

    private Messages() {
    }

    // --- Cart ---
    /** Sepet bulunamadı. */
    public static final String CART_NOT_FOUND = "Sepet bulunamadı.";

    /** Aynı müşteri + fatura hesabı için zaten aktif bir sepet var. */
    public static final String CART_ALREADY_EXISTS =
            "Bu müşteri ve fatura hesabı için zaten aktif bir sepet bulunuyor.";

    // --- CartItem ---
    /** Sepet satırı bulunamadı. */
    public static final String CART_ITEM_NOT_FOUND = "Sepet satırı bulunamadı.";

    /** Aynı kampanya sepete birden çok kez eklenemez (paket bütünlüğü). */
    public static final String CAMPAIGN_ALREADY_IN_CART =
            "Bu kampanya sepete zaten eklenmiş.";

    // --- Sepetten siparişe geçiş Saga'sı (cart-service doğrulayıcı) nedenleri ---
    /** Saga: siparişe konu sepet bulunamadı (yok/silinmiş). */
    public static final String SAGA_CART_NOT_FOUND =
            "Siparişe konu sepet bulunamadı.";

    /** Saga: sepet boş (onaylanmış/aktif satırı yok), sipariş oluşturulamaz. */
    public static final String SAGA_CART_EMPTY =
            "Sepette onaylanmış ürün bulunmadığından sipariş oluşturulamaz.";
}
