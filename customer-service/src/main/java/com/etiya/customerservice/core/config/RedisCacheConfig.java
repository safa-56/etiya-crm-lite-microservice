package com.etiya.customerservice.core.config;

import com.etiya.customerservice.business.dtos.responses.IndividualCustomerResponse;
import com.etiya.customerservice.core.constants.CacheNames;
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
 * <p>Her cache, sakladığı somut tipe bağlı (type-bound) bir serializer kullanır.
 * Bu sayede global "default typing"e (polymorphic tip bilgisi gömme) gerek kalmaz;
 * özellikle üst seviyesi {@code List} olan değerler (ör. {@code getAll})
 * güvenilir şekilde round-trip edilir. Global default typing kullanıldığında
 * koleksiyonlar için yazma/okuma tarafları uyuşmadığından cache'ten okurken
 * {@code SerializationException} alınıyordu.
 */
@Configuration
public class RedisCacheConfig {

    private static final TypeFactory TYPE_FACTORY = TypeFactory.createDefaultInstance();

    /**
     * Tek bir {@link IndividualCustomerResponse} saklayan cache'ler için serializer.
     */
    private RedisSerializer<Object> individualCustomerSerializer() {
        JavaType type = TYPE_FACTORY.constructType(IndividualCustomerResponse.class);
        return new JacksonJsonRedisSerializer<>(type);
    }

    /**
     * {@code List<IndividualCustomerResponse>} saklayan cache'ler için serializer.
     */
    private RedisSerializer<Object> individualCustomerListSerializer() {
        JavaType listType = TYPE_FACTORY.constructCollectionType(List.class, IndividualCustomerResponse.class);
        return new JacksonJsonRedisSerializer<>(listType);
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
                .cacheDefaults(baseConfig(Duration.ofMinutes(10), individualCustomerSerializer()))
                .withInitialCacheConfigurations(Map.of(
                        CacheNames.INDIVIDUAL_CUSTOMERS,
                        baseConfig(Duration.ofMinutes(10), individualCustomerSerializer()),
                        CacheNames.INDIVIDUAL_CUSTOMER_LIST,
                        baseConfig(Duration.ofMinutes(2), individualCustomerListSerializer())
                ))
                .build();
    }
}
