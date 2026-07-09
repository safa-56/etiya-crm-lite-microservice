package com.etiya.accountservice.business.constants;

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

    /** Fatura hesabı bulunamadı. */
    public static final String BILLING_ACCOUNT_NOT_FOUND = "Fatura hesabı bulunamadı.";

    /** Hesap numarası zaten kullanımda. */
    public static final String ACCOUNT_NUMBER_ALREADY_EXISTS = "Bu hesap numarası zaten kayıtlı.";

    /**
     * Aktif ürünü olan hesap silinemez.
     *
     * <p>Kabul kriteri gereği bu mesaj birebir bu metinle döner (değiştirilmemeli).
     */
    public static final String BILLING_ACCOUNT_HAS_ACTIVE_PRODUCTS =
            "The billing account cannot be deleted because it has active products.";

    // --- Ürün satışı Saga'sı (doğrulayıcı adım) ---

    /** Ürün satışı Saga'sı: hedef fatura hesabı bulunamadı (telafi nedeni). */
    public static final String SAGA_BILLING_ACCOUNT_NOT_FOUND = "Fatura hesabı bulunamadı.";

    /** Ürün satışı Saga'sı: fatura hesabı aktif (ACTIVE) durumda değil (telafi nedeni). */
    public static final String SAGA_BILLING_ACCOUNT_NOT_ACTIVE =
            "Fatura hesabı aktif olmadığı için ürün bağlanamaz.";
}
