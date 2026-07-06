package com.etiya.gatewayserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Etiya CRM Lite - API Gateway sunucusu.
 *
 * Spring Cloud Gateway, sistemin tek giriş noktasıdır (edge service).
 * Dış istekleri Eureka'dan keşfedilen arka mikroservislere (customer,
 * account vb.) yönlendirir; cross-cutting concern'leri (routing,
 * load balancing, ileride auth/rate-limit/CORS) tek yerde toplar.
 *
 * Eureka client bağımlılığı classpath'te olduğu için DiscoveryClient
 * otomatik etkinleşir; ayrı bir @EnableDiscoveryClient gerekmez.
 */
@SpringBootApplication
public class GatewayServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServerApplication.class, args);
    }
}
