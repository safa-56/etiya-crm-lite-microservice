package com.etiya.cartservice.entities.enums;

/**
 * Sepet satırının türü — sepete ekleme yolunu ayırt eder (FR-014).
 *
 * <ul>
 *   <li>{@link #OFFER}    : katalogdan doğrudan seçilen tek bir ürün teklifi
 *       ({@code product_offer_id} dolu, {@code campaign_id} boş).</li>
 *   <li>{@link #CAMPAIGN} : içinde birden çok teklif bulunan, tek paket fiyatıyla
 *       bir bütün olarak eklenen kampanya ({@code campaign_id} dolu,
 *       {@code product_offer_id} boş).</li>
 * </ul>
 */
public enum CartItemType {

    /** Katalogdan doğrudan seçilen ürün teklifi. */
    OFFER,

    /** Paket (bundle) olarak eklenen kampanya. */
    CAMPAIGN
}
