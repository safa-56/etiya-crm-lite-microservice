package com.etiya.customerservice.apiController;

import com.etiya.customerservice.business.abstracts.CustomerContactInfoService;
import com.etiya.customerservice.business.dtos.requests.CreateContactInfoRequest;
import com.etiya.customerservice.business.dtos.requests.UpdateContactInfoRequest;
import com.etiya.customerservice.business.dtos.responses.ContactInfoResponse;
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
 * Müşteri iletişim bilgisi (CustomerContactInfo) REST uçları — apiController katmanı.
 *
 * <p>İşi doğrudan {@link CustomerContactInfoService} soyutlamasına delege eder;
 * iş kuralları/hata yönetimi alt katmanlarda ele alınır.
 */
@RestController
@RequestMapping("/api/v1/contact-infos")
public class CustomerContactInfosController {

    private final CustomerContactInfoService customerContactInfoService;

    public CustomerContactInfosController(CustomerContactInfoService customerContactInfoService) {
        this.customerContactInfoService = customerContactInfoService;
    }

    /** Yeni iletişim bilgisi oluşturur (gövdedeki {@code customerId} ile müşteriye bağlanır). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactInfoResponse add(@Valid @RequestBody CreateContactInfoRequest request) {
        return customerContactInfoService.add(request);
    }

    /** Id ile tek bir iletişim bilgisini getirir. */
    @GetMapping("/{id}")
    public ContactInfoResponse getById(@PathVariable Long id) {
        return customerContactInfoService.getById(id);
    }

    /** Tüm aktif iletişim bilgilerini listeler. */
    @GetMapping
    public List<ContactInfoResponse> getAll() {
        return customerContactInfoService.getAll();
    }

    /** Var olan bir iletişim bilgisini günceller. */
    @PutMapping("/{id}")
    public ContactInfoResponse update(@PathVariable Long id,
                                      @Valid @RequestBody UpdateContactInfoRequest request) {
        return customerContactInfoService.update(id, request);
    }

    /** İletişim bilgisini soft-delete ile pasifleştirir. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerContactInfoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
