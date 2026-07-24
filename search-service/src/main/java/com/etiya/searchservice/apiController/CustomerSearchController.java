package com.etiya.searchservice.apiController;

import com.etiya.searchservice.business.abstracts.CustomerSearchService;
import com.etiya.searchservice.business.dtos.requests.CustomerSearchRequest;
import com.etiya.searchservice.business.dtos.responses.CustomerSearchResponse;
import com.etiya.searchservice.business.dtos.responses.PagedResponse;
import com.etiya.searchservice.business.rules.CustomerSearchBusinessRules;
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
    /**
     * Sayfalama doğrulaması için. Ham {@code page}/{@code size} query değerleri yalnızca
     * bu katmanda görülebilir — {@code Pageable} çözücüsü geçersiz değerleri alt katmana
     * ulaşmadan sabitlediğinden, kontrol iş katmanına devredilemez.
     */
    private final CustomerSearchBusinessRules rules;

    public CustomerSearchController(CustomerSearchService customerSearchService,
                                    CustomerSearchBusinessRules rules) {
        this.customerSearchService = customerSearchService;
        this.rules = rules;
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
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @PageableDefault(size = 50, sort = "customerId", direction = Sort.Direction.ASC) Pageable pageable) {

        // page/size hem burada ham hâliyle hem de pageable içinde çözülmüş hâliyle alınır;
        // geçersiz değerler pageable'da kaybolduğu için doğrulama ham değerler üzerinden yapılır.
        rules.validatePagination(page, size);

        CustomerSearchRequest request = new CustomerSearchRequest(
                segment, idNumber, customerId, accountNumber, gsm, firstName, lastName, orderNumber);
        return customerSearchService.search(request, pageable);
    }
}
