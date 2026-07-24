package com.etiya.accountservice.core.config;

import com.etiya.accountservice.business.dtos.responses.BillingAccountResponse;
import com.etiya.accountservice.business.dtos.responses.PagedResponse;
import com.etiya.accountservice.core.constants.CacheNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.type.TypeFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Redis tabanlı cacheleme konfigürasyonu.
 *
 * <p>Değerler JSON olarak serialize edilir; {@code null} değerler cache'lenmez.
 * Cache bazında TTL tanımlanır. Cache'ler RedisInsight üzerinden
 * ({@code redis:6379}) izlenebilir.
 *
 * <p>Her cache, sakladığı somut tipe bağlı (type-bound) bir serializer kullanır; bu sayede
 * global "default typing"e (polymorphic tip bilgisi gömme) gerek kalmaz. Global default typing,
 * üst seviyesi {@code List} olan ve elemanları {@code record} (final) olan değerlerde
 * yazma/okuma tarafları uyuşmadığından cache'ten okurken {@code SerializationException}
 * fırlatıyordu (müşteri hesapları ekranını 500 ile çökertiyordu). Bu yüzden by-customer listesi
 * ayrı bir cache'e alınıp ({@link CacheNames#BILLING_ACCOUNTS_BY_CUSTOMER}) tipe-bağlı serialize edilir.
 *
 * <p>Ek olarak {@link #errorHandler()}, eski/uyumsuz bir cache girdisi okunamazsa hatayı
 * yükseltmek yerine <b>cache-miss</b> gibi ele alır: metot yeniden çalışır ve doğru formatta
 * değeri geri yazar (kendi kendini iyileştirir). Böylece serileştirme formatı değişse bile
 * kalıntı girdiler isteği çökertmez.
 */
@Configuration
public class RedisCacheConfig implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheConfig.class);

    private static final TypeFactory TYPE_FACTORY = TypeFactory.createDefaultInstance();

    /** Tek bir {@link BillingAccountResponse} saklayan cache'ler için serializer. */
    private RedisSerializer<Object> billingAccountSerializer() {
        JavaType type = TYPE_FACTORY.constructType(BillingAccountResponse.class);
        return new JacksonJsonRedisSerializer<>(type);
    }

    /** {@code List<BillingAccountResponse>} saklayan cache için serializer (by-customer). */
    private RedisSerializer<Object> billingAccountListSerializer() {
        JavaType listType = TYPE_FACTORY.constructCollectionType(List.class, BillingAccountResponse.class);
        return new JacksonJsonRedisSerializer<>(listType);
    }

    /** {@code PagedResponse<BillingAccountResponse>} saklayan cache için serializer (getAll). */
    private RedisSerializer<Object> billingAccountPageSerializer() {
        JavaType pageType = TYPE_FACTORY.constructParametricType(
                PagedResponse.class, BillingAccountResponse.class);
        return new JacksonJsonRedisSerializer<>(pageType);
    }

    private RedisCacheConfiguration baseConfig(Duration ttl, RedisSerializer<Object> valueSerializer) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(valueSerializer));
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(baseConfig(Duration.ofMinutes(10), billingAccountSerializer()))
                .withInitialCacheConfigurations(Map.of(
                        CacheNames.BILLING_ACCOUNTS,
                        baseConfig(Duration.ofMinutes(10), billingAccountSerializer()),
                        CacheNames.BILLING_ACCOUNT_LIST,
                        baseConfig(Duration.ofMinutes(2), billingAccountPageSerializer()),
                        CacheNames.BILLING_ACCOUNTS_BY_CUSTOMER,
                        baseConfig(Duration.ofMinutes(2), billingAccountListSerializer())
                ))
                .build();
    }

    /**
     * Cache okuma/yazma hatalarını yükseltmek yerine loglayıp yutar; okuma hatası cache-miss
     * gibi ele alınır (metot yeniden çalışır, sonuç doğru formatta yeniden yazılır). Böylece
     * eski/uyumsuz kalıntı girdiler isteği 500 ile çökertmez.
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache okuma hatası — cache-miss olarak ele alınıyor: cache={}, key={}, sebep={}",
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Cache yazma hatası — yok sayılıyor: cache={}, key={}, sebep={}",
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache silme hatası — yok sayılıyor: cache={}, key={}, sebep={}",
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Cache temizleme hatası — yok sayılıyor: cache={}, sebep={}",
                        cache.getName(), exception.getMessage());
            }
        };
    }
}
