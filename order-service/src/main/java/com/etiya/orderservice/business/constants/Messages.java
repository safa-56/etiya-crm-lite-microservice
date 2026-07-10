package com.etiya.orderservice.business.constants;

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

    // --- Order (order-service tarafı, istemciye dönen) ---
    /** Sipariş bulunamadı. */
    public static final String ORDER_NOT_FOUND = "Sipariş bulunamadı.";

    /** Verilen sepet için hâlâ süren bir sipariş var (aynı sepet iki kez submit edilemez). */
    public static final String ORDER_ALREADY_EXISTS_FOR_CART =
            "Bu sepet için zaten devam eden bir sipariş bulunuyor.";

    // --- Saga doğrulama nedenleri (cart-service doğrulayıcı tarafında üretilir) ---
    /** Saga: siparişe konu sepet bulunamadı (yok/silinmiş). */
    public static final String SAGA_CART_NOT_FOUND =
            "Siparişe konu sepet bulunamadı.";

    /** Saga: sepet boş (onaylanmış/aktif satırı yok), sipariş oluşturulamaz. */
    public static final String SAGA_CART_EMPTY =
            "Sepette onaylanmış ürün bulunmadığından sipariş oluşturulamaz.";
}
