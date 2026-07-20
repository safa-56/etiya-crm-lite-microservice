package com.etiya.customerservice.business.constants;

/**
 * Party modeli referans veri sabitleri (Bounded Context Ownership sözleşmesi).
 *
 * <p><b>Sahiplik kuralı:</b> customer-service, genel referans tablolarının
 * ({@code general_status}, {@code general_type}) yalnızca aşağıdaki
 * {@code ENTITY_*} dilimlerine sahiptir. {@code PROD}, {@code CUST_ORD},
 * {@code CUST_ACCT} gibi diğer dilimler ilgili servislerin kendi
 * veritabanlarında durur; burada tutulmaz ve buradan okunmaz.
 *
 * <p><b>Sınır geçiş kuralı:</b> Bir durum/tip servis sınırını geçmesi gerekirse
 * (ör. outbox olay gövdesinde), surrogate id ile değil buradaki
 * <b>{@code *_CODE}</b> sabitleriyle taşınır. Id'ler bu context'in iç detayıdır
 * ve ETL/seed ile değişebilir; kısa kodlar semantik olarak stabildir.
 */
public final class PartyReferenceCodes {

    private PartyReferenceCodes() {
    }

    // --- Sahip olunan dilimler (legacy ENT_CODE_NAME) -------------------------

    /** Party dilimi (legacy {@code ENT_CODE_NAME = PARTY}). */
    public static final String ENTITY_PARTY = "PARTY";

    /** Party rolü dilimi (legacy {@code ENT_CODE_NAME = PARTY_ROLE}). */
    public static final String ENTITY_PARTY_ROLE = "PARTY_ROLE";

    /** Birey dilimi (legacy {@code ENT_CODE_NAME = IND}, {@code ENT_NAME = INDIVIDUAL}). */
    public static final String ENTITY_INDIVIDUAL = "IND";

    /** Adres dilimi (durum FK'si için). */
    public static final String ENTITY_ADDRESS = "ADDRESS";

    /** Müşteri iletişim bilgisi dilimi (durum FK'si için). */
    public static final String ENTITY_CONTACT_INFO = "CUST_CONTACT";

    /**
     * Party tipi dilimi (legacy {@code GNL_TP.ENT_CODE_NAME = CAM_PARTY_TYPE}).
     *
     * <p>Party tipleri GNL_ST'deki {@code PARTY} diliminden ayrı bir isim alanında
     * durur: durumlar {@code PARTY} altında, tipler {@code CAM_PARTY_TYPE} altındadır.
     * Bu yüzden {@link #ENTITY_PARTY} ile karıştırılmamalıdır.
     */
    public static final String ENTITY_CAM_PARTY_TYPE = "CAM_PARTY_TYPE";

    // --- Durum kısa kodları (legacy GNL_ST.SHRT_CODE) ------------------------

    /** Aktif durum. */
    public static final String STATUS_ACTIVE_CODE = "ACTV";

    /** Pasif durum. */
    public static final String STATUS_PASSIVE_CODE = "PASS";

    /** Silinmiş durum (soft-delete'in iş karşılığı). */
    public static final String STATUS_DELETED_CODE = "DEL";

    // --- Tip kısa kodları (legacy GNL_TP.SHRT_CODE, CAM_PARTY_TYPE dilimi) ---

    /** Bireysel party tipi (legacy GNL_TP id 164). */
    public static final String PARTY_TYPE_INDIVIDUAL_CODE = "INDV";

    /** Kurumsal party tipi (legacy GNL_TP id 163). */
    public static final String PARTY_TYPE_ORGANIZATION_CODE = "ORG";

    // --- Rol tipi kısa kodları (legacy PARTY_ROLE_TP.SHRT_CODE) --------------

    /** Müşteri rolü. */
    public static final String PARTY_ROLE_TYPE_CUSTOMER_CODE = "CUST";
}
