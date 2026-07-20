package com.etiya.cartservice.business.constants;

/**
 * Referans veri sabitleri (Bounded Context Ownership sözleşmesi).
 *
 * <p><b>Sahiplik kuralı:</b> cart-service, {@code general_status} tablosunun yalnızca
 * {@code CART}, {@code CART_ITEM} ve {@code CART_ITEM_LINE} dilimlerine sahiptir.
 */
public final class CartReferenceCodes {

    private CartReferenceCodes() {
    }

    // --- Sahip olunan dilimler -----------------------------------------------

    /** Sepet dilimi. */
    public static final String ENTITY_CART = "CART";

    /** Sepet satırı dilimi. */
    public static final String ENTITY_CART_ITEM = "CART_ITEM";

    /** Sepet satırı paket içeriği dilimi. */
    public static final String ENTITY_CART_ITEM_LINE = "CART_ITEM_LINE";

    // --- Durum kısa kodları --------------------------------------------------

    /** Aktif. */
    public static final String STATUS_ACTIVE_CODE = "ACTV";

    /** Beklemede (saga: PENDING karşılığı). */
    public static final String STATUS_PENDING_CODE = "PNDG";

    /** İptal (saga telafisi: CANCELLED karşılığı). */
    public static final String STATUS_CANCELLED_CODE = "CNCL";

    /** Silinmiş (soft-delete). */
    public static final String STATUS_DELETED_CODE = "DEL";
}
