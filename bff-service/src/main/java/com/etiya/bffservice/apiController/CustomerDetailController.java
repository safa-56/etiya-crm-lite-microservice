package com.etiya.bffservice.apiController;

import com.etiya.bffservice.business.CustomerDetailAggregator;
import com.etiya.bffservice.business.dtos.CustomerDetailResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Müşteri detay ekranı için BFF ucu.
 *
 * <p>Gateway üzerinden dış yol: {@code GET /bff-service/api/v1/customer-detail/{id}}
 * (StripPrefix=1 sonrası iç yol {@code /api/v1/customer-detail/{id}}).
 */
@RestController
@RequestMapping("/api/v1/customer-detail")
public class CustomerDetailController {

    private final CustomerDetailAggregator aggregator;

    public CustomerDetailController(CustomerDetailAggregator aggregator) {
        this.aggregator = aggregator;
    }

    /** Bir müşterinin detayını (kimlik + iletişim + adres + hesaplar) tek yanıtta döner. */
    @GetMapping("/{id}")
    public CustomerDetailResponse getCustomerDetail(@PathVariable Long id) {
        return aggregator.getCustomerDetail(id);
    }
}
