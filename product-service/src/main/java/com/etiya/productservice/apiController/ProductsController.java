package com.etiya.productservice.apiController;

import com.etiya.productservice.business.abstracts.ProductService;
import com.etiya.productservice.business.dtos.requests.CreateProductRequest;
import com.etiya.productservice.business.dtos.responses.PagedResponse;
import com.etiya.productservice.business.dtos.responses.ProductDetailResponse;
import com.etiya.productservice.business.dtos.responses.ProductResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Ürün (Product) REST uçları — apiController katmanı.
 *
 * <p>Ürün satışı (create) ve silme (soft-delete) işlemleri Outbox+Debezium ile
 * account-service'e olay yayınlar. FR-013 için {@code ?accountId=} ile fatura
 * hesabına bağlı ürün detayları salt okunur listelenir.
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductsController {

    private final ProductService productService;

    public ProductsController(ProductService productService) {
        this.productService = productService;
    }

    /** Ürün teklifini bir fatura hesabına satar (ProductCreated olayı yayınlanır). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse add(@Valid @RequestBody CreateProductRequest request) {
        return productService.add(request);
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @GetMapping
    public PagedResponse<ProductResponse> getAll(Pageable pageable) {
        return productService.getAll(pageable);
    }

    /** FR-013: fatura hesabına bağlı ürün detayları (salt okunur). */
    @GetMapping("/details")
    public List<ProductDetailResponse> getDetailsByAccount(@RequestParam Long accountId) {
        return productService.getDetailsByAccount(accountId);
    }

    /** Ürünü siler (soft-delete; ProductDeleted olayı yayınlanır). */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
