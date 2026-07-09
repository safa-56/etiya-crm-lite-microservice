package com.etiya.productservice.core.config;

import com.etiya.productservice.core.constants.CacheNames;
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
        // Tekil kayıt cache'leri daha uzun (10 dk), liste cache'leri daha kısa (2 dk)
        // TTL ile yapılandırılır (liste sık değişebilir).
        RedisCacheConfiguration entityTtl = baseConfig(Duration.ofMinutes(10));
        RedisCacheConfiguration listTtl = baseConfig(Duration.ofMinutes(2));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(entityTtl)
                .withInitialCacheConfigurations(Map.of(
                        CacheNames.PRODUCT_SPECS, entityTtl,
                        CacheNames.PRODUCT_SPEC_LIST, listTtl,
                        CacheNames.PRODUCT_OFFERS, entityTtl,
                        CacheNames.PRODUCT_OFFER_LIST, listTtl,
                        CacheNames.CATALOGS, entityTtl,
                        CacheNames.CATALOG_LIST, listTtl,
                        CacheNames.CAMPAIGNS, entityTtl,
                        CacheNames.CAMPAIGN_LIST, listTtl,
                        CacheNames.PRODUCTS, entityTtl
                ))
                .build();
    }
}
