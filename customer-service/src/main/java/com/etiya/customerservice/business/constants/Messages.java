package com.etiya.customerservice.business.constants;

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

    /** Bireysel müşteri bulunamadı. */
    public static final String INDIVIDUAL_CUSTOMER_NOT_FOUND = "Bireysel müşteri bulunamadı.";

    /** Müşteri bulunamadı. */
    public static final String CUSTOMER_NOT_FOUND = "Müşteri bulunamadı.";

    /** Adres bulunamadı. */
    public static final String ADDRESS_NOT_FOUND = "Adres bulunamadı.";

    /** İletişim bilgisi bulunamadı. */
    public static final String CONTACT_INFO_NOT_FOUND = "İletişim bilgisi bulunamadı.";

    /** Verilen e-posta zaten kayıtlı. */
    public static final String EMAIL_ALREADY_EXISTS = "Bu e-posta adresi zaten kayıtlı.";

    /**
     * Girilen TC kimlik numarası (Nationality ID) başka bir müşteriye ait.
     *
     * <p>FR-003 (ACC-08) ve FR-004 (ACC-08) kabul kriteri gereği bu mesaj birebir
     * bu metinle döner (değiştirilmemeli).
     */
    public static final String NATIONALITY_ID_ALREADY_EXISTS =
            "A customer is already exist with this Nationality ID.";

    /** Doğum tarihi gelecekte olamaz. */
    public static final String BIRTH_DATE_CANNOT_BE_IN_FUTURE = "Doğum tarihi gelecekte olamaz.";
}
