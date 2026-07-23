package com.etiya.orderservice.business.constants;

/**
 * İş katmanı mesaj <b>anahtarları</b> (i18n). Gerçek metin isteğin diline göre
 * {@code messages*.properties} üzerinden çözülür. {@code SAGA_*} sabitleri
 * servisler arası event yükünde taşındığı için sabit metin olarak kalır.
 */
public final class Messages {

    private Messages() {
    }

    // --- Order (order-service tarafı, istemciye dönen) ---
    /** Sipariş bulunamadı. */
    public static final String ORDER_NOT_FOUND = "order.notFound";

    /** Verilen sepet için hâlâ süren bir sipariş var (aynı sepet iki kez submit edilemez). */
    public static final String ORDER_ALREADY_EXISTS_FOR_CART = "order.alreadyExistsForCart";

    /** Ard arda denemelere rağmen boşta bir sipariş numarası bulunamadı. */
    public static final String ORDER_NUMBER_GENERATION_FAILED = "order.numberGenerationFailed";

    /** Beklenen referans veri satırı bulunamadı. Parametreli: {@code {0}} = detay. */
    public static final String REFERENCE_DATA_NOT_FOUND = "reference.data.notFound";

    // --- Saga doğrulama nedenleri (event yükü; locale'e göre çevrilmez) ---
    /** Saga: siparişe konu sepet bulunamadı (yok/silinmiş). */
    public static final String SAGA_CART_NOT_FOUND =
            "Siparişe konu sepet bulunamadı.";

    /** Saga: sepet boş (onaylanmış/aktif satırı yok), sipariş oluşturulamaz. */
    public static final String SAGA_CART_EMPTY =
            "Sepette onaylanmış ürün bulunmadığından sipariş oluşturulamaz.";
}
