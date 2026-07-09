package com.etiya.cartservice.business.abstracts;

import com.etiya.cartservice.business.dtos.requests.AddCampaignToCartRequest;
import com.etiya.cartservice.business.dtos.requests.AddOfferToCartRequest;
import com.etiya.cartservice.business.dtos.requests.CreateCartRequest;
import com.etiya.cartservice.business.dtos.responses.CartResponse;
import com.etiya.cartservice.business.dtos.responses.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Sepet (Cart) iş servisi soyutlaması.
 *
 * <p>Sepet CRUD'una ek olarak FR-014'ün iki ekleme yolunu sunar: katalogdan doğrudan
 * teklif ekleme ({@link #addOffer}) ve kampanya (paket) ekleme ({@link #addCampaign}).
 */
public interface CartService {

    /** Yeni bir sepet oluşturur (müşteri + fatura hesabı için). */
    CartResponse create(CreateCartRequest request);

    /** Id ile tek bir sepeti (satırları ve toplamıyla) getirir. */
    CartResponse getById(Long id);

    /** Aktif sepetleri sayfalı listeler. */
    PagedResponse<CartResponse> getAll(Pageable pageable);

    /** Bir müşteriye ait tüm aktif sepetleri getirir. */
    List<CartResponse> getByCustomer(Long customerId);

    /** Sepete katalogdan doğrudan bir ürün teklifi ekler (1. yol). */
    CartResponse addOffer(Long cartId, AddOfferToCartRequest request);

    /** Sepete bir kampanyayı (paket) tek fiyatla bir bütün olarak ekler (2. yol). */
    CartResponse addCampaign(Long cartId, AddCampaignToCartRequest request);

    /** Sepetten bir satırı çıkarır (soft-delete). */
    CartResponse removeItem(Long cartId, Long itemId);

    /** Sepeti (ve tüm satırlarını) siler/boşaltır (soft-delete). */
    void delete(Long id);
}
