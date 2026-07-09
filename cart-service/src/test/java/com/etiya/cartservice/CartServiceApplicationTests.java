package com.etiya.cartservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Uygulama bağlamının (Spring context) hatasız ayağa kalktığını doğrular.
 *
 * <p>'test' profili hermetiktir: in-memory H2 (şema entity'lerden üretilir),
 * Kafka consumer'ları ve Eureka kapalı, cache in-memory. Bu test; JPA eşlemeleri
 * (Cart, CartItem, outbox/inbox, offer/campaign projeksiyonları), bean wiring,
 * MapStruct mapper ve konfigürasyonların tutarlılığını uçtan uca kontrol eder.
 */
@SpringBootTest
@ActiveProfiles("test")
class CartServiceApplicationTests {

    @Test
    void contextLoads() {
        // Context yüklenebiliyorsa (schema + bean wiring) test geçer.
    }
}
