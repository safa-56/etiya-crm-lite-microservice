package com.etiya.bffservice.business.dtos;

import com.etiya.bffservice.business.dtos.downstream.BillingAccountResponse;
import com.etiya.bffservice.business.dtos.downstream.IndividualCustomerResponse;

import java.util.List;

/**
 * Müşteri detay ekranının tek yanıtı (BFF aggregation çıktısı).
 *
 * <p>{@code customer} customer-service'ten (iletişim + adresler iç içe), {@code accounts}
 * account-service'ten gelir. Frontend bu iki parçayı kendi görünüm modeline eşler;
 * böylece tarayıcı birden çok servise ayrı ayrı gitmez.
 */
public record CustomerDetailResponse(
        IndividualCustomerResponse customer,
        List<BillingAccountResponse> accounts
) {
}
