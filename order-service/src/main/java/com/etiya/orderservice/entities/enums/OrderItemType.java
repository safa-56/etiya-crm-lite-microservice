package com.etiya.orderservice.entities.enums;

/**
 * Sipariş satırının türü — sepetten gelen kalemin kaynağını ayırt eder.
 *
 * <p>Sepet satırı (cart-service {@code CartItemType}) türüyle birebir eşleşir; sipariş
 * onaylandığında sepet satırının türü sipariş satırına snapshot'lanır.
 *
 * <ul>
 *   <li>{@link #OFFER}    : katalogdan doğrudan seçilmiş tek bir ürün teklifi
 *       ({@code product_offer_id} dolu, {@code campaign_id} boş).</li>
 *   <li>{@link #CAMPAIGN} : tek paket fiyatıyla eklenmiş bir kampanya
 *       ({@code campaign_id} dolu, {@code product_offer_id} boş).</li>
 * </ul>
 */
public enum OrderItemType {

    /** Katalogdan doğrudan seçilen ürün teklifi. */
    OFFER,

    /** Paket (bundle) olarak eklenen kampanya. */
    CAMPAIGN
}
