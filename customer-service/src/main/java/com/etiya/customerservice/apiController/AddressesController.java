package com.etiya.customerservice.apiController;

import com.etiya.customerservice.business.abstracts.AddressService;
import com.etiya.customerservice.business.dtos.requests.CreateAddressRequest;
import com.etiya.customerservice.business.dtos.requests.UpdateAddressRequest;
import com.etiya.customerservice.business.dtos.responses.AddressResponse;
import jakarta.validation.Valid;
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

import java.util.List;

/**
 * Adres (Address) REST uçları — apiController katmanı.
 *
 * <p>İşi doğrudan {@link AddressService} soyutlamasına delege eder; iş
 * kuralları/hata yönetimi alt katmanlarda ele alınır.
 */
@RestController
@RequestMapping("/api/v1/addresses")
public class AddressesController {

    private final AddressService addressService;

    public AddressesController(AddressService addressService) {
        this.addressService = addressService;
    }

    /** Yeni adres oluşturur (gövdedeki {@code customerId} ile müşteriye bağlanır). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse add(@Valid @RequestBody CreateAddressRequest request) {
        return addressService.add(request);
    }

    /** Id ile tek bir adresi getirir. */
    @GetMapping("/{id}")
    public AddressResponse getById(@PathVariable Long id) {
        return addressService.getById(id);
    }

    /** Tüm aktif adresleri listeler. */
    @GetMapping
    public List<AddressResponse> getAll() {
        return addressService.getAll();
    }

    /** Var olan bir adresi günceller. */
    @PutMapping("/{id}")
    public AddressResponse update(@PathVariable Long id,
                                  @Valid @RequestBody UpdateAddressRequest request) {
        // Yol değişkeni (path) ile gövdedeki id'yi tutarlı hale getir.
        UpdateAddressRequest normalized = new UpdateAddressRequest(
                id, request.city(), request.street(), request.houseNumber(),
                request.addressDescription(), request.isPrimary());
        return addressService.update(normalized);
    }

    /** Adresi soft-delete ile pasifleştirir. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
