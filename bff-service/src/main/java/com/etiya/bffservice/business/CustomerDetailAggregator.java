package com.etiya.bffservice.business;

import com.etiya.bffservice.business.dtos.CustomerDetailResponse;
import com.etiya.bffservice.business.dtos.downstream.BillingAccountResponse;
import com.etiya.bffservice.business.dtos.downstream.IndividualCustomerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.function.Supplier;

/**
 * Müşteri detay verisini iki servisten toplayan iş katmanı.
 *
 * <p>Müşteri (kimlik/iletişim/adres) customer-service'ten, fatura hesapları
 * account-service'ten çekilir ve tek DTO'da birleştirilir. Çağrılar kullanıcının
 * token'ıyla (relay) yapılır; downstream 401/403/404 hataları RestClient tarafından
 * exception olarak yükseltilir ve global handler'da çevrilir.
 *
 * <p><b>Dayanıklılık:</b> downstream çağrıları geçici hatalarda (bağlantı hatası / 5xx)
 * yeniden denenir. Eureka'da bir servisin eski (ölü) instance'ı henüz düşmemişse
 * load-balancer round-robin ile bazen ona yönlenip bağlantı hatası alır; yeniden deneme
 * bir sonraki (canlı) instance'ı seçtiğinden detay ekranının "bazen açılmama" durumu giderilir.
 */
@Service
public class CustomerDetailAggregator {

    private static final Logger log = LoggerFactory.getLogger(CustomerDetailAggregator.class);

    /** Geçici downstream hatalarında toplam deneme sayısı (ilk çağrı + yeniden denemeler). */
    private static final int MAX_ATTEMPTS = 3;

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
        log.info("[customer-detail] Aggregation başladı: customerId={}", customerId);

        IndividualCustomerResponse customer = callWithRetry(
                "customer-service GET /api/v1/individual-customers/" + customerId,
                () -> customerServiceRestClient.get()
                        .uri("/api/v1/individual-customers/{id}", customerId)
                        .retrieve()
                        .body(IndividualCustomerResponse.class));
        log.info("[customer-detail] Müşteri alındı: customerId={}, ad={} {}, iletişim={}, adres={}",
                customerId,
                customer != null ? customer.firstName() : null,
                customer != null ? customer.lastName() : null,
                customer != null && customer.contactInfos() != null ? customer.contactInfos().size() : 0,
                customer != null && customer.addresses() != null ? customer.addresses().size() : 0);

        List<BillingAccountResponse> accounts = callWithRetry(
                "account-service GET /api/v1/billing-accounts/by-customer/" + customerId,
                () -> accountServiceRestClient.get()
                        .uri("/api/v1/billing-accounts/by-customer/{customerId}", customerId)
                        .retrieve()
                        .body(ACCOUNT_LIST));

        int accountCount = accounts == null ? 0 : accounts.size();
        log.info("[customer-detail] Aggregation tamamlandı: customerId={}, hesapSayısı={}",
                customerId, accountCount);

        return new CustomerDetailResponse(customer, accounts == null ? List.of() : accounts);
    }

    /**
     * Bir downstream çağrısını geçici hatalarda yeniden dener.
     *
     * <p>İstemci hataları (4xx, ör. 404 müşteri bulunamadı) yeniden denenmez; hemen yükseltilir.
     * Bağlantı hatası ({@link ResourceAccessException}) ve sunucu hatası (5xx,
     * {@link HttpServerErrorException} — load-balancer'ın "instance yok" için döndürdüğü 503 dahil)
     * geçici kabul edilir ve bir sonraki instance'a yeniden denenir.
     */
    private <T> T callWithRetry(String description, Supplier<T> call) {
        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                log.info("[customer-detail] Downstream çağrı (deneme {}/{}): {}",
                        attempt, MAX_ATTEMPTS, description);
                T result = call.get();
                log.info("[customer-detail] Downstream çağrı BAŞARILI (deneme {}/{}): {}",
                        attempt, MAX_ATTEMPTS, description);
                return result;
            } catch (HttpClientErrorException e) {
                // 4xx (ör. 404): yeniden deneme anlamsız, olduğu gibi yükselt.
                log.warn("[customer-detail] Downstream İSTEMCİ HATASI ({}): {} — yeniden denenmeyecek",
                        e.getStatusCode(), description);
                throw e;
            } catch (ResourceAccessException | HttpServerErrorException e) {
                lastError = e;
                log.warn("[customer-detail] Downstream çağrı BAŞARISIZ (deneme {}/{}): {} — sebep: {}",
                        attempt, MAX_ATTEMPTS, description, e.getMessage());
            }
        }
        log.error("[customer-detail] Downstream çağrı {} denemede de başarısız oldu: {}",
                MAX_ATTEMPTS, description, lastError);
        throw lastError;
    }
}
