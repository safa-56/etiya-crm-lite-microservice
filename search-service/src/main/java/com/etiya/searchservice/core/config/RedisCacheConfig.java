package com.etiya.searchservice.core.config;

import com.etiya.searchservice.business.dtos.responses.CustomerSearchResponse;
import com.etiya.searchservice.business.dtos.responses.PagedResponse;
import com.etiya.searchservice.core.constants.CacheNames;
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
import java.util.Map;

/**
 * Redis tabanlı cacheleme konfigürasyonu (customer-service ile aynı desen).
 *
 * <p>Arama sonucu ({@code PagedResponse<CustomerSearchResponse>}) sakladığı somut
 * tipe bağlı (type-bound) bir serializer ile JSON olarak round-trip edilir; global
 * "default typing"e gerek kalmaz. Liste sorgusu volatil olduğundan kısa TTL (~2 dk)
 * kullanılır; indeks değiştiğinde cache tamamen boşaltılır (projeksiyon manager'ı).
 */
@Configuration
public class RedisCacheConfig {

    private static final TypeFactory TYPE_FACTORY = TypeFactory.createDefaultInstance();

    /** {@code PagedResponse<CustomerSearchResponse>} saklayan cache için serializer. */
    private RedisSerializer<Object> customerSearchSerializer() {
        JavaType type = TYPE_FACTORY.constructParametricType(
                PagedResponse.class, CustomerSearchResponse.class);
        return new JacksonJsonRedisSerializer<>(type);
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
                .cacheDefaults(baseConfig(Duration.ofMinutes(2), customerSearchSerializer()))
                .withInitialCacheConfigurations(Map.of(
                        CacheNames.CUSTOMER_SEARCH,
                        baseConfig(Duration.ofMinutes(2), customerSearchSerializer())
                ))
                .build();
    }
}
