package com.etiya.customerservice.business.constants;

/**
 * İş katmanı mesaj <b>anahtarları</b> (i18n).
 *
 * <p>Kullanıcıya/istemciye dönen iş mesajları artık doğrudan metin değil,
 * {@code messages*.properties} dosyalarındaki mesaj anahtarlarıdır. Gerçek metin
 * (Türkçe/İngilizce), isteğin {@code Accept-Language} başlığına göre
 * {@code GlobalExceptionHandler} içinde {@code MessageSource} ile çözülür.
 *
 * <p><b>Not:</b> {@code SAGA_*} sabitleri istisnadır. Bunlar servisler arası olay
 * (event) yükünde taşınan telafi nedenleridir; asenkron üretildikleri için istek
 * (HTTP) dil bağlamı yoktur. Bu yüzden bunlar anahtar değil, sabit metin olarak kalır.
 */
public final class Messages {

    private Messages() {
    }

    /** Bireysel müşteri bulunamadı. */
    public static final String INDIVIDUAL_CUSTOMER_NOT_FOUND = "customer.individual.notFound";

    /** Müşteri bulunamadı. */
    public static final String CUSTOMER_NOT_FOUND = "customer.notFound";

    /** Adres bulunamadı. */
    public static final String ADDRESS_NOT_FOUND = "address.notFound";

    /** İletişim bilgisi bulunamadı. */
    public static final String CONTACT_INFO_NOT_FOUND = "contactInfo.notFound";

    /** Verilen e-posta zaten kayıtlı. */
    public static final String EMAIL_ALREADY_EXISTS = "customer.email.alreadyExists";

    /**
     * Girilen TC kimlik numarası (Nationality ID) başka bir müşteriye ait.
     *
     * <p>FR-003 (ACC-08) ve FR-004 (ACC-08) kabul kriteri gereği bu mesaj her iki
     * dilde de birebir aynı İngilizce metinle döner (değiştirilmemeli).
     */
    public static final String NATIONALITY_ID_ALREADY_EXISTS = "customer.nationalityId.alreadyExists";

    /** Doğum tarihi gelecekte olamaz. */
    public static final String BIRTH_DATE_CANNOT_BE_IN_FUTURE = "customer.birthDate.notInFuture";

    // --- Referans veri (Party modeli lookup'ları) ---

    /**
     * Beklenen referans veri satırı bulunamadı. Parametreli mesaj: {@code {0}} = detay.
     *
     * <p>Tipik sebep: {@code data.sql} seed'i çalışmamış ya da ilgili
     * {@code ENT_CODE_NAME}/{@code SHRT_CODE} satırı pasifleştirilmiş.
     */
    public static final String REFERENCE_DATA_NOT_FOUND = "reference.data.notFound";

    // --- Saga (fatura hesabı oluşturma) doğrulama nedenleri ---
    // Servisler arası event yükünde taşınır; locale'e göre çevrilmez (sabit metin).

    /** Saga: fatura hesabının bağlanmak istendiği müşteri aktif değil/yok. */
    public static final String SAGA_CUSTOMER_NOT_FOUND =
            "Customer not found or inactive.";

    /** Saga: seçilen adres müşteriye ait değil veya aktif değil. */
    public static final String SAGA_ADDRESS_NOT_FOUND =
            "Address does not belong to the customer or is inactive.";
}
