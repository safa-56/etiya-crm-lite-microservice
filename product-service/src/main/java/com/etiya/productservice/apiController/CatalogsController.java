package com.etiya.productservice.apiController;

import com.etiya.productservice.business.abstracts.CatalogService;
import com.etiya.productservice.business.dtos.requests.CreateCatalogRequest;
import com.etiya.productservice.business.dtos.requests.UpdateCatalogRequest;
import com.etiya.productservice.business.dtos.responses.CatalogResponse;
import com.etiya.productservice.business.dtos.responses.PagedResponse;
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
 * Katalog (Catalog) REST uçları — apiController katmanı.
 */
@RestController
@RequestMapping("/api/v1/catalogs")
public class CatalogsController {

    private final CatalogService catalogService;

    public CatalogsController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogResponse add(@Valid @RequestBody CreateCatalogRequest request) {
        return catalogService.add(request);
    }

    @GetMapping("/{id}")
    public CatalogResponse getById(@PathVariable Long id) {
        return catalogService.getById(id);
    }

    @GetMapping
    public PagedResponse<CatalogResponse> getAll(Pageable pageable) {
        return catalogService.getAll(pageable);
    }

    @PutMapping("/{id}")
    public CatalogResponse update(@PathVariable Long id,
                                  @Valid @RequestBody UpdateCatalogRequest request) {
        return catalogService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        catalogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
