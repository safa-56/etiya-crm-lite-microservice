package com.etiya.cartservice.business.constants;

/**
 * İş katmanı mesaj <b>anahtarları</b> (i18n). Gerçek metin isteğin diline göre
 * {@code messages*.properties} üzerinden çözülür. {@code SAGA_*} sabitleri
 * servisler arası event yükünde taşındığı için sabit metin olarak kalır.
 */
public final class Messages {

    private Messages() {
    }

    // --- Cart ---
    /** Sepet bulunamadı. */
    public static final String CART_NOT_FOUND = "cart.notFound";

    /** Aynı müşteri + fatura hesabı için zaten aktif bir sepet var. */
    public static final String CART_ALREADY_EXISTS = "cart.alreadyExists";

    // --- CartItem ---
    /** Sepet satırı bulunamadı. */
    public static final String CART_ITEM_NOT_FOUND = "cartItem.notFound";

    /** Aynı kampanya sepete birden çok kez eklenemez (paket bütünlüğü). */
    public static final String CAMPAIGN_ALREADY_IN_CART = "campaign.alreadyInCart";

    // --- Sepetten siparişe geçiş Saga'sı (event yükü; locale'e göre çevrilmez) ---
    /** Saga: siparişe konu sepet bulunamadı (yok/silinmiş). */
    public static final String SAGA_CART_NOT_FOUND =
            "Siparişe konu sepet bulunamadı.";

    /** Saga: sepet boş (onaylanmış/aktif satırı yok), sipariş oluşturulamaz. */
    public static final String SAGA_CART_EMPTY =
            "Sepette onaylanmış ürün bulunmadığından sipariş oluşturulamaz.";
}
