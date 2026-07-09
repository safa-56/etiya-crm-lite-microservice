package com.etiya.productservice.apiController;

import com.etiya.productservice.business.abstracts.ProductOfferService;
import com.etiya.productservice.business.dtos.requests.CreateProductOfferRequest;
import com.etiya.productservice.business.dtos.requests.UpdateProductOfferRequest;
import com.etiya.productservice.business.dtos.responses.PagedResponse;
import com.etiya.productservice.business.dtos.responses.ProductOfferResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ürün teklifi (ProductOffer) REST uçları — apiController katmanı.
 *
 * <p>Teklif Seçimi ekranı (FR-014) için {@code ?catalogId=} ve {@code ?campaignId=}
 * ile katalog/kampanya bazlı sayfalı arama sağlar.
 */
@RestController
@RequestMapping("/api/v1/product-offers")
public class ProductOffersController {

    private final ProductOfferService productOfferService;

    public ProductOffersController(ProductOfferService productOfferService) {
        this.productOfferService = productOfferService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductOfferResponse add(@Valid @RequestBody CreateProductOfferRequest request) {
        return productOfferService.add(request);
    }

    @GetMapping("/{id}")
    public ProductOfferResponse getById(@PathVariable Long id) {
        return productOfferService.getById(id);
    }

    /**
     * Teklifleri sayfalı listeler. {@code catalogId} veya {@code campaignId}
     * verilirse ilgili kataloga/kampanyaya bağlı teklifler döner (Teklif Seçimi
     * sekmeleri); ikisi de verilmezse tüm aktif teklifler döner.
     */
    @GetMapping
    public PagedResponse<ProductOfferResponse> getAll(
            @RequestParam(required = false) Long catalogId,
            @RequestParam(required = false) Long campaignId,
            Pageable pageable) {
        if (catalogId != null) {
            return productOfferService.getByCatalog(catalogId, pageable);
        }
        if (campaignId != null) {
            return productOfferService.getByCampaign(campaignId, pageable);
        }
        return productOfferService.getAll(pageable);
    }

    @PutMapping("/{id}")
    public ProductOfferResponse update(@PathVariable Long id,
                                       @Valid @RequestBody UpdateProductOfferRequest request) {
        return productOfferService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productOfferService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
