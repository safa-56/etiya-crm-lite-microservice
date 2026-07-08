package com.etiya.accountservice.apiController;

import com.etiya.accountservice.business.abstracts.BillingAccountService;
import com.etiya.accountservice.business.dtos.requests.CreateBillingAccountRequest;
import com.etiya.accountservice.business.dtos.requests.UpdateBillingAccountRequest;
import com.etiya.accountservice.business.dtos.responses.BillingAccountResponse;
import com.etiya.accountservice.business.dtos.responses.PagedResponse;
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
 * Fatura hesabı (BillingAccount) REST uçları — apiController katmanı.
 *
 * <p>İşi doğrudan {@link BillingAccountService} soyutlamasına delege eder;
 * iş kuralları/hata yönetimi/cacheleme alt katmanlarda ele alınır.
 */
@RestController
@RequestMapping("/api/v1/billing-accounts")
public class BillingAccountsController {

    private final BillingAccountService billingAccountService;

    public BillingAccountsController(BillingAccountService billingAccountService) {
        this.billingAccountService = billingAccountService;
    }

    /** Yeni fatura hesabı oluşturur (type=Billing Account, status=Active sistemce atanır). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BillingAccountResponse add(@Valid @RequestBody CreateBillingAccountRequest request) {
        return billingAccountService.add(request);
    }

    /** Id ile tek bir fatura hesabını getirir. */
    @GetMapping("/{id}")
    public BillingAccountResponse getById(@PathVariable Long id) {
        return billingAccountService.getById(id);
    }

    /**
     * Aktif fatura hesaplarını sayfalı listeler.
     *
     * <p>{@code ?page=0&size=20&sort=accountName,asc} gibi standart sayfalama
     * parametrelerini kabul eder; yanıt toplam sayfa/kayıt bilgisini içerir.
     */
    @GetMapping
    public PagedResponse<BillingAccountResponse> getAll(Pageable pageable) {
        return billingAccountService.getAll(pageable);
    }

    /** Var olan bir fatura hesabını günceller. */
    @PutMapping("/{id}")
    public BillingAccountResponse update(@PathVariable Long id,
                                         @Valid @RequestBody UpdateBillingAccountRequest request) {
        // Yol değişkeni (path) ile gövdedeki id'yi tutarlı hale getir.
        UpdateBillingAccountRequest normalized = new UpdateBillingAccountRequest(
                id, request.accountName(), request.accountDescription(), request.addressId(),
                request.accountNumber(), request.orderNumber());
        return billingAccountService.update(normalized);
    }

    /**
     * Fatura hesabını siler.
     *
     * <p>Aktif ürünü varsa iş hatası (400) döner ve hesap silinmez; aksi halde
     * fiziksel silme yerine durum PASSIVE yapılır (soft-delete).
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        billingAccountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
