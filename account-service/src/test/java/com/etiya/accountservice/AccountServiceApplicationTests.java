package com.etiya.accountservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Uygulama bağlamının (Spring context) hatasız ayağa kalktığını doğrular.
 *
 * <p>'test' profili hermetiktir: in-memory H2 (şema entity'lerden üretilir),
 * Kafka consumer'ları ve Eureka kapalı, cache in-memory. Bu test; JPA eşlemeleri
 * (BillingAccount, outbox/inbox), bean wiring, MapStruct mapper ve
 * konfigürasyonların tutarlılığını uçtan uca kontrol eder.
 */
@SpringBootTest
@ActiveProfiles("test")
class AccountServiceApplicationTests {

    @Test
    void contextLoads() {
        // Context yüklenebiliyorsa (schema + bean wiring) test geçer.
    }
}
