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
     * Fatura hesabının bağlanmak istendiği müşteri, yerel projeksiyonda
     * (Kafka ile beslenen read-model) aktif olarak bulunamadı.
     */
    public static final String CUSTOMER_NOT_FOUND = "Müşteri bulunamadı.";

    /**
     * Seçilen adres, verilen müşteriye ait değil veya yerel projeksiyonda yok
     * (müşteri/adres olayı henüz tüketilmemiş olabilir).
     */
    public static final String ADDRESS_NOT_FOUND_FOR_CUSTOMER =
            "Adres bu müşteriye ait değil veya bulunamadı.";

    /**
     * Aktif ürünü olan hesap silinemez.
     *
     * <p>Kabul kriteri gereği bu mesaj birebir bu metinle döner (değiştirilmemeli).
     */
    public static final String BILLING_ACCOUNT_HAS_ACTIVE_PRODUCTS =
            "The billing account cannot be deleted because it has active products.";
}
