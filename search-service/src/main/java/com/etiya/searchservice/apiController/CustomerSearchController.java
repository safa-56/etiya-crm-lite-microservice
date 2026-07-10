package com.etiya.searchservice.apiController;

import com.etiya.searchservice.business.abstracts.CustomerSearchService;
import com.etiya.searchservice.business.dtos.requests.CustomerSearchRequest;
import com.etiya.searchservice.business.dtos.responses.CustomerSearchResponse;
import com.etiya.searchservice.business.dtos.responses.PagedResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Müşteri arama REST ucu (FR-002) — apiController katmanı.
 *
 * <p>Tek uç: {@code GET /api/v1/search/customers}. Tüm arama parametreleri
 * opsiyoneldir; sayfalama {@code page}/{@code size} (varsayılan 50 — ACC-19) ve
 * {@code sort} (varsayılan {@code customerId,asc} — ACC-20) ile yönetilir.
 * İş/validasyon/cache alt katmanlarda ele alınır.
 */
@RestController
@RequestMapping("/api/v1/search")
public class CustomerSearchController {

    private final CustomerSearchService customerSearchService;

    public CustomerSearchController(CustomerSearchService customerSearchService) {
        this.customerSearchService = customerSearchService;
    }

    /** Kriterlere göre müşteri arar (eşleşme yoksa boş liste — ACC-24). */
    @GetMapping("/customers")
    public PagedResponse<CustomerSearchResponse> searchCustomers(
            @RequestParam(defaultValue = "B2C") String segment,
            @RequestParam(required = false) String idNumber,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) String gsm,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String orderNumber,
            @PageableDefault(size = 50, sort = "customerId", direction = Sort.Direction.ASC) Pageable pageable) {

        CustomerSearchRequest request = new CustomerSearchRequest(
                segment, idNumber, customerId, accountNumber, gsm, firstName, lastName, orderNumber);
        return customerSearchService.search(request, pageable);
    }
}
