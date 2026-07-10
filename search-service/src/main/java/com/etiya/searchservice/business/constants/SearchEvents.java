package com.etiya.searchservice.business.constants;

/**
 * Tüketilen olay akışlarıyla ilgili sabitler (olay tipleri ve hesap durumları).
 *
 * <p>search-service kendi olayını yayınlamaz; bu sabitler yalnızca gelen
 * customer/account olaylarının nasıl yorumlanacağını (upsert/remove) belirler.
 */
public final class SearchEvents {

    private SearchEvents() {
    }

    // --- customer olay tipleri (crm.Customer.events) ---
    public static final String CUSTOMER_CREATED = "CustomerCreated";
    public static final String CUSTOMER_UPDATED = "CustomerUpdated";
    public static final String CUSTOMER_DELETED = "CustomerDeleted";

    // --- account hesap durumları (crm.Account.events, BillingAccountEventPayload.accountStatus) ---
    /** Hesap aktif → account/order numaralarını indekse EKLE. */
    public static final String ACCOUNT_STATUS_ACTIVE = "ACTIVE";
    /** Hesap iptal edildi (saga telafisi) → numaraları ÇIKAR. */
    public static final String ACCOUNT_STATUS_CANCELLED = "CANCELLED";
    /** Hesap silindi (soft-delete) → numaraları ÇIKAR. */
    public static final String ACCOUNT_STATUS_PASSIVE = "PASSIVE";
}
