package com.etiya.productservice.business.constants;

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

    // --- ProductSpec ---
    public static final String PRODUCT_SPEC_NOT_FOUND = "Ürün teknik özelliği bulunamadı.";

    // --- ProductOffer ---
    public static final String PRODUCT_OFFER_NOT_FOUND = "Ürün teklifi bulunamadı.";
    public static final String PRODUCT_OFFER_DATE_RANGE_INVALID =
            "Teklif bitiş tarihi başlangıç tarihinden önce olamaz.";

    // --- Catalog ---
    public static final String CATALOG_NOT_FOUND = "Katalog bulunamadı.";

    // --- Campaign ---
    public static final String CAMPAIGN_NOT_FOUND = "Kampanya bulunamadı.";
    public static final String CAMPAIGN_DUPLICATE_OFFER =
            "Aynı ürün teklifi bir kampanyada birden çok kez yer alamaz.";

    // --- Product ---
    public static final String PRODUCT_NOT_FOUND = "Ürün bulunamadı.";

    // --- Sepete ekleme Saga'sı (doğrulayıcı adım) ---

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
