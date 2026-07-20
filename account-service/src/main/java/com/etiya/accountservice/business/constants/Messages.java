package com.etiya.accountservice.business.constants;

/**
 * İş katmanı mesaj <b>anahtarları</b> (i18n). Gerçek metin isteğin diline göre
 * {@code messages*.properties} üzerinden çözülür. {@code SAGA_*} sabitleri
 * servisler arası event yükünde taşındığı için sabit metin olarak kalır.
 */
public final class Messages {

    private Messages() {
    }

    /** Fatura hesabı bulunamadı. */
    public static final String BILLING_ACCOUNT_NOT_FOUND = "billingAccount.notFound";

    /** Hesap numarası zaten kullanımda. */
    public static final String ACCOUNT_NUMBER_ALREADY_EXISTS = "account.number.alreadyExists";

    /** Beklenen referans veri satırı bulunamadı. Parametreli: {@code {0}} = detay. */
    public static final String REFERENCE_DATA_NOT_FOUND = "reference.data.notFound";

    /**
     * Aktif ürünü olan hesap silinemez.
     *
     * <p>Kabul kriteri gereği bu mesaj her iki dilde de birebir aynı İngilizce
     * metinle döner (değiştirilmemeli).
     */
    public static final String BILLING_ACCOUNT_HAS_ACTIVE_PRODUCTS = "billingAccount.hasActiveProducts";

    // --- Ürün satışı Saga'sı (event yükü; locale'e göre çevrilmez) ---

    /** Ürün satışı Saga'sı: hedef fatura hesabı bulunamadı (telafi nedeni). */
    public static final String SAGA_BILLING_ACCOUNT_NOT_FOUND = "Fatura hesabı bulunamadı.";

    /** Ürün satışı Saga'sı: fatura hesabı aktif (ACTIVE) durumda değil (telafi nedeni). */
    public static final String SAGA_BILLING_ACCOUNT_NOT_ACTIVE =
            "Fatura hesabı aktif olmadığı için ürün bağlanamaz.";
}
