package com.etiya.bffservice.business;

import com.etiya.bffservice.business.dtos.CustomerDetailResponse;
import com.etiya.bffservice.business.dtos.downstream.BillingAccountResponse;
import com.etiya.bffservice.business.dtos.downstream.IndividualCustomerResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Müşteri detay verisini iki servisten toplayan iş katmanı.
 *
 * <p>Müşteri (kimlik/iletişim/adres) customer-service'ten, fatura hesapları
 * account-service'ten çekilir ve tek DTO'da birleştirilir. Çağrılar kullanıcının
 * token'ıyla (relay) yapılır; downstream 401/403/404 hataları RestClient tarafından
 * exception olarak yükseltilir ve global handler'da çevrilir.
 */
@Service
public class CustomerDetailAggregator {

    private static final ParameterizedTypeReference<List<BillingAccountResponse>> ACCOUNT_LIST =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient customerServiceRestClient;
    private final RestClient accountServiceRestClient;

    public CustomerDetailAggregator(RestClient customerServiceRestClient,
                                    RestClient accountServiceRestClient) {
        this.customerServiceRestClient = customerServiceRestClient;
        this.accountServiceRestClient = accountServiceRestClient;
    }

    /** Müşteriyi ve fatura hesaplarını çekip birleştirir. */
    public CustomerDetailResponse getCustomerDetail(Long customerId) {
        IndividualCustomerResponse customer = customerServiceRestClient.get()
                .uri("/api/v1/individual-customers/{id}", customerId)
                .retrieve()
                .body(IndividualCustomerResponse.class);

        List<BillingAccountResponse> accounts = accountServiceRestClient.get()
                .uri("/api/v1/billing-accounts/by-customer/{customerId}", customerId)
                .retrieve()
                .body(ACCOUNT_LIST);

        return new CustomerDetailResponse(customer, accounts == null ? List.of() : accounts);
    }
}
