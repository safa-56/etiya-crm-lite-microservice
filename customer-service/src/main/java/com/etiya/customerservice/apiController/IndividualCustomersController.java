package com.etiya.customerservice.apiController;

import com.etiya.customerservice.business.abstracts.IndividualCustomerService;
import com.etiya.customerservice.business.dtos.requests.CreateIndividualCustomerRequest;
import com.etiya.customerservice.business.dtos.requests.UpdateIndividualCustomerRequest;
import com.etiya.customerservice.business.dtos.responses.IndividualCustomerResponse;
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
 * Bireysel müşteri (IndividualCustomer) REST uçları — apiController katmanı.
 *
 * <p>İşi doğrudan {@link IndividualCustomerService} soyutlamasına delege eder;
 * iş kuralları/hata yönetimi/cacheleme alt katmanlarda ele alınır.
 */
@RestController
@RequestMapping("/api/v1/individual-customers")
public class IndividualCustomersController {

    private final IndividualCustomerService individualCustomerService;

    public IndividualCustomersController(IndividualCustomerService individualCustomerService) {
        this.individualCustomerService = individualCustomerService;
    }

    /** Yeni bireysel müşteri oluşturur. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IndividualCustomerResponse add(@Valid @RequestBody CreateIndividualCustomerRequest request) {
        return individualCustomerService.add(request);
    }

    /** Id ile tek bir bireysel müşteriyi getirir. */
    @GetMapping("/{id}")
    public IndividualCustomerResponse getById(@PathVariable Long id) {
        return individualCustomerService.getById(id);
    }

    /** Tüm aktif bireysel müşterileri listeler. */
    @GetMapping
    public List<IndividualCustomerResponse> getAll() {
        return individualCustomerService.getAll();
    }

    /** Var olan bir bireysel müşteriyi günceller. */
    @PutMapping("/{id}")
    public IndividualCustomerResponse update(@PathVariable Long id,
                                             @Valid @RequestBody UpdateIndividualCustomerRequest request) {
        // Yol değişkeni (path) ile gövdedeki id'yi tutarlı hale getir.
        UpdateIndividualCustomerRequest normalized = new UpdateIndividualCustomerRequest(
                id, request.firstName(), request.secondName(), request.lastName(), request.birthDate(),
                request.fatherName(), request.motherName(), request.nationalityId(),
                request.genderType(), request.contactInfos(), request.addresses());
        return individualCustomerService.update(normalized);
    }

    /** Bireysel müşteriyi soft-delete ile pasifleştirir. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        individualCustomerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
