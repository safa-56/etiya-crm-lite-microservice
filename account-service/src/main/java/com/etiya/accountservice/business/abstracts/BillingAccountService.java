package com.etiya.accountservice.business.abstracts;

import com.etiya.accountservice.business.dtos.requests.CreateBillingAccountRequest;
import com.etiya.accountservice.business.dtos.requests.UpdateBillingAccountRequest;
import com.etiya.accountservice.business.dtos.responses.BillingAccountResponse;
import com.etiya.accountservice.business.dtos.responses.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Fatura hesabı iş servisi (business abstraction). CRUD işlemlerini tanımlar.
 */
public interface BillingAccountService {

    BillingAccountResponse add(CreateBillingAccountRequest request);

    BillingAccountResponse getById(Long id);

    /** Aktif fatura hesaplarını sayfalı listeler (kabul kriteri: sayfalama). */
    PagedResponse<BillingAccountResponse> getAll(Pageable pageable);

    /** Bir müşteriye bağlı tüm aktif fatura hesaplarını listeler (Customer Account ekranı). */
    List<BillingAccountResponse> getByCustomerId(Long customerId);

    BillingAccountResponse update(Long id, UpdateBillingAccountRequest request);

    void delete(Long id);
}
