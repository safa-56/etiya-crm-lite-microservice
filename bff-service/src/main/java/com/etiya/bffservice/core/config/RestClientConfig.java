package com.etiya.bffservice.core.config;

import org.springframework.cloud.client.loadbalancer.DeferringLoadBalancerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Downstream servislere senkron erişim için {@link RestClient} yapılandırması.
 *
 * <p>Load balancing (Eureka {@code lb}) için, {@code @LoadBalanced} bir builder
 * bean'i TANIMLAMIYORUZ: aksi halde o tek builder Eureka client'ının kendi RestClient'ı
 * olarak da seçilir ve Eureka registry'sine ({@code localhost:8761}) giderken load-balancer
 * devreye girip "localhost"u servis sanar (açılışta döngüsel bağımlılık). Bunun yerine
 * Boot'un varsayılan builder'ı altyapıda (Eureka) load-balanced OLMADAN kalır; load-balancer
 * interceptor'ı yalnızca aşağıdaki iki iş istemcisine elle eklenir.
 *
 * <p>Her downstream çağrıda gelen isteğin {@code Authorization} başlığı aynen iletilir
 * (token relay); böylece customer/account servisleri çağrıyı yapan kullanıcıyla doğrular.
 */
@Configuration
public class RestClientConfig {

    /**
     * Düz (load-balanced OLMAYAN) RestClient builder. Eureka client dahil tüm altyapı
     * bunu güvenle kullanır (lb interceptor yok → açılışta döngü yok). Load balancing,
     * yalnızca iş istemcilerinde elle eklenir.
     */
    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    /** customer-service'e erişen istemci (individual-customers uçları). */
    @Bean
    RestClient customerServiceRestClient(RestClient.Builder builder,
                                         DeferringLoadBalancerInterceptor loadBalancerInterceptor) {
        // clone(): paylaşılan varsayılan builder'ı mutasyona uğratmamak için.
        return builder.clone()
                .baseUrl("http://customer-service")
                .requestInterceptor(loadBalancerInterceptor)
                .requestInterceptor(bearerTokenRelayInterceptor())
                .build();
    }

    /** account-service'e erişen istemci (billing-accounts uçları). */
    @Bean
    RestClient accountServiceRestClient(RestClient.Builder builder,
                                        DeferringLoadBalancerInterceptor loadBalancerInterceptor) {
        return builder.clone()
                .baseUrl("http://account-service")
                .requestInterceptor(loadBalancerInterceptor)
                .requestInterceptor(bearerTokenRelayInterceptor())
                .build();
    }

    /**
     * Gelen isteğin {@code Authorization} başlığını downstream çağrıya kopyalar.
     * İstek bağlamı yoksa (ör. arka plan görevi) başlık eklenmez.
     */
    private static ClientHttpRequestInterceptor bearerTokenRelayInterceptor() {
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
