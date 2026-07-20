package com.etiya.productservice.business.constants;

/**
 * İş katmanı mesaj <b>anahtarları</b> (i18n). Gerçek metin isteğin diline göre
 * {@code messages*.properties} üzerinden çözülür. {@code SAGA_*} sabitleri
 * servisler arası event yükünde taşındığı için sabit metin olarak kalır.
 */
public final class Messages {

    private Messages() {
    }

    // --- ProductSpec ---
    public static final String PRODUCT_SPEC_NOT_FOUND = "productSpec.notFound";

    // --- ProductOffer ---
    public static final String PRODUCT_OFFER_NOT_FOUND = "productOffer.notFound";
    public static final String PRODUCT_OFFER_DATE_RANGE_INVALID = "productOffer.dateRangeInvalid";

    // --- Catalog ---
    public static final String CATALOG_NOT_FOUND = "catalog.notFound";

    // --- Campaign ---
    public static final String CAMPAIGN_NOT_FOUND = "campaign.notFound";
    public static final String CAMPAIGN_DUPLICATE_OFFER = "campaign.duplicateOffer";

    // --- Product ---
    public static final String PRODUCT_NOT_FOUND = "product.notFound";

    /** Beklenen referans veri satırı bulunamadı. Parametreli: {@code {0}} = detay. */
    public static final String REFERENCE_DATA_NOT_FOUND = "reference.data.notFound";

    // --- Sepete ekleme Saga'sı (event yükü; locale'e göre çevrilmez) ---

    /** Sepet saga'sı: eklenmek istenen ürün teklifi bulunamadı/aktif değil (telafi nedeni). */
    public static final String SAGA_CART_PRODUCT_OFFER_NOT_FOUND =
            "Ürün teklifi bulunamadı veya aktif değil.";

    /** Sepet saga'sı: eklenmek istenen kampanya bulunamadı/aktif değil (telafi nedeni). */
    public static final String SAGA_CART_CAMPAIGN_NOT_FOUND =
            "Kampanya bulunamadı veya aktif değil.";

    /** Sepet saga'sı: satır türü tanınmadı (telafi nedeni). */
    public static final String SAGA_CART_ITEM_TYPE_UNKNOWN =
            "Sepet satırı türü tanınmadı.";
}
