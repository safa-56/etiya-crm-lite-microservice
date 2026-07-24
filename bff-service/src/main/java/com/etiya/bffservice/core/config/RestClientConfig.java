package com.etiya.bffservice.core.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Downstream servislere senkron erişim için {@link RestClient} yapılandırması.
 *
 * <p>{@link LoadBalanced} işaretli builder, {@code http://<servis-adı>} host'unu
 * Eureka üzerinden çözer (client-side load balancing). Her downstream çağrıda,
 * gelen isteğin {@code Authorization} başlığı aynen iletilir (token relay); böylece
 * customer-service/account-service, çağrıyı yapan kullanıcının kimliğiyle doğrular.
 */
@Configuration
public class RestClientConfig {

    /** Eureka çözümlemeli (lb) RestClient builder. Her enjeksiyonda yeni örnek. */
    @Bean
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    /** customer-service'e erişen istemci (individual-customers uçları). */
    @Bean
    RestClient customerServiceRestClient(@LoadBalanced RestClient.Builder builder) {
        // clone(): paylaşılan builder'ı mutasyona uğratmamak için (baseUrl/interceptor izole).
        return builder.clone()
                .baseUrl("http://customer-service")
                .requestInterceptor(bearerTokenRelayInterceptor())
                .build();
    }

    /** account-service'e erişen istemci (billing-accounts uçları). */
    @Bean
    RestClient accountServiceRestClient(@LoadBalanced RestClient.Builder builder) {
        return builder.clone()
                .baseUrl("http://account-service")
                .requestInterceptor(bearerTokenRelayInterceptor())
                .build();
    }

    /**
     * Gelen isteğin {@code Authorization} başlığını downstream çağrıya kopyalar.
     * İstek bağlamı yoksa (ör. arka plan görevi) başlık eklenmez.
     */
    private static org.springframework.http.client.ClientHttpRequestInterceptor bearerTokenRelayInterceptor() {
        return (request, body, execution) -> {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
                String authorization = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
                if (authorization != null && !authorization.isBlank()) {
                    request.getHeaders().set(HttpHeaders.AUTHORIZATION, authorization);
                }
            }
            return execution.execute(request, body);
        };
    }
}
