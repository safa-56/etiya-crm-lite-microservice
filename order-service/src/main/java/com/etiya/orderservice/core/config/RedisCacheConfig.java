package com.etiya.orderservice.core.config;

import com.etiya.orderservice.core.constants.CacheNames;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

/**
 * Redis tabanlı cacheleme konfigürasyonu.
 *
 * <p>Değerler JSON olarak (tip bilgisiyle) serialize edilir; {@code null}
 * değerler cache'lenmez. Cache bazında TTL tanımlanır. Cache'ler RedisInsight
 * üzerinden ({@code redis:6379}) izlenebilir.
 */
@Configuration
public class RedisCacheConfig {

    /**
     * JSON serileştirici (Jackson 3 tabanlı). Java 8 tarih/zaman
     * (LocalDate/LocalDateTime) desteği Jackson 3'te otomatik gelir.
     *
     * <p>Cache'ten geri okurken somut tipleri çözebilmek için tip bilgisi
     * gömülür. {@code enableUnsafeDefaultTyping}, yalnızca güvenilen (internal)
     * Redis verisi için uygundur.
     */
    private GenericJacksonJsonRedisSerializer jsonSerializer() {
        return GenericJacksonJsonRedisSerializer.builder()
                .enableUnsafeDefaultTyping()
                .build();
    }

    private RedisCacheConfiguration baseConfig(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer()));
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(baseConfig(Duration.ofMinutes(10)))
                .withInitialCacheConfigurations(Map.of(
                        // Sipariş, oluşturulduktan sonra kısa süre PENDING görünür ve saga ile
                        // kesinleşir; bu geçiş sık cache güncellemesi getirdiğinden tekil sipariş
                        // cache'i kısa TTL alır.
                        CacheNames.ORDERS, baseConfig(Duration.ofMinutes(5)),
                        CacheNames.ORDER_LIST, baseConfig(Duration.ofMinutes(2))
                ))
                .build();
    }
}
