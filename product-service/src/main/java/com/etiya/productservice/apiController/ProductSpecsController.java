package com.etiya.productservice.apiController;

import com.etiya.productservice.business.abstracts.ProductSpecService;
import com.etiya.productservice.business.dtos.requests.CreateProductSpecRequest;
import com.etiya.productservice.business.dtos.requests.UpdateProductSpecRequest;
import com.etiya.productservice.business.dtos.responses.PagedResponse;
import com.etiya.productservice.business.dtos.responses.ProductSpecResponse;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ürün teknik özelliği (ProductSpec) REST uçları — apiController katmanı.
 *
 * <p>İşi doğrudan {@link ProductSpecService} soyutlamasına delege eder; iş
 * kuralları/hata yönetimi/cacheleme alt katmanlarda ele alınır.
 */
@RestController
@RequestMapping("/api/v1/product-specs")
public class ProductSpecsController {

    private final ProductSpecService productSpecService;

    public ProductSpecsController(ProductSpecService productSpecService) {
        this.productSpecService = productSpecService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductSpecResponse add(@Valid @RequestBody CreateProductSpecRequest request) {
        return productSpecService.add(request);
    }

    @GetMapping("/{id}")
    public ProductSpecResponse getById(@PathVariable Long id) {
        return productSpecService.getById(id);
    }

    @GetMapping
    public PagedResponse<ProductSpecResponse> getAll(Pageable pageable) {
        return productSpecService.getAll(pageable);
    }

    @PutMapping("/{id}")
    public ProductSpecResponse update(@PathVariable Long id,
                                      @Valid @RequestBody UpdateProductSpecRequest request) {
        return productSpecService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productSpecService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
