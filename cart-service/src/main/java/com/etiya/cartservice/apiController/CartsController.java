package com.etiya.cartservice.apiController;

import com.etiya.cartservice.business.abstracts.CartService;
import com.etiya.cartservice.business.dtos.requests.AddCampaignToCartRequest;
import com.etiya.cartservice.business.dtos.requests.AddOfferToCartRequest;
import com.etiya.cartservice.business.dtos.requests.CreateCartRequest;
import com.etiya.cartservice.business.dtos.responses.CartResponse;
import com.etiya.cartservice.business.dtos.responses.PagedResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Sepet (Cart) REST uçları — apiController katmanı.
 *
 * <p>İşi doğrudan {@link CartService} soyutlamasına delege eder; iş kuralları/hata
 * yönetimi/cacheleme alt katmanlarda ele alınır. Sepete ürün eklemenin iki yolu iki
 * ayrı uç ile sunulur: {@code POST .../items/offers} (katalogdan teklif) ve
 * {@code POST .../items/campaigns} (kampanya/paket).
 */
@RestController
@RequestMapping("/api/v1/carts")
public class CartsController {

    private final CartService cartService;

    public CartsController(CartService cartService) {
        this.cartService = cartService;
    }

    /** Yeni bir sepet oluşturur (müşteri + fatura hesabı için). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse create(@Valid @RequestBody CreateCartRequest request) {
        return cartService.create(request);
    }

    /** Id ile tek bir sepeti (satırları ve toplamıyla) getirir. */
    @GetMapping("/{id}")
    public CartResponse getById(@PathVariable Long id) {
        return cartService.getById(id);
    }

    /** Aktif sepetleri sayfalı listeler. */
    @GetMapping
    public PagedResponse<CartResponse> getAll(Pageable pageable) {
        return cartService.getAll(pageable);
    }

    /** Bir müşteriye ait tüm aktif sepetleri getirir. */
    @GetMapping("/customer/{customerId}")
    public List<CartResponse> getByCustomer(@PathVariable Long customerId) {
        return cartService.getByCustomer(customerId);
    }

    /** Sepete katalogdan doğrudan bir ürün teklifi ekler (FR-014, 1. yol). */
    @PostMapping("/{cartId}/items/offers")
    public CartResponse addOffer(@PathVariable Long cartId,
                                 @Valid @RequestBody AddOfferToCartRequest request) {
        return cartService.addOffer(cartId, request);
    }

    /** Sepete bir kampanyayı (paket) tek fiyatla bir bütün olarak ekler (FR-014, 2. yol). */
    @PostMapping("/{cartId}/items/campaigns")
    public CartResponse addCampaign(@PathVariable Long cartId,
                                    @Valid @RequestBody AddCampaignToCartRequest request) {
        return cartService.addCampaign(cartId, request);
    }

    /** Sepetten bir satırı çıkarır (soft-delete). */
    @DeleteMapping("/{cartId}/items/{itemId}")
    public CartResponse removeItem(@PathVariable Long cartId, @PathVariable Long itemId) {
        return cartService.removeItem(cartId, itemId);
    }

    /** Sepeti (ve tüm satırlarını) siler/boşaltır (soft-delete). */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cartService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
