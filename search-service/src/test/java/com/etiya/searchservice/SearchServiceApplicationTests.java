package com.etiya.searchservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Hermetik smoke test: 'test' profili (H2, Kafka/Eureka kapalı, in-memory cache)
 * ile Spring context'inin sorunsuz ayağa kalktığını doğrular.
 */
@SpringBootTest
@ActiveProfiles("test")
class SearchServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
