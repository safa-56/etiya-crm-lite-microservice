package com.etiya.customerservice.core.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.etiya.customerservice.core.constants.CacheNames;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
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

    /** JSON serileştirme için Java 8 tarih/zaman (LocalDate/LocalDateTime) destekli mapper. */
    private GenericJackson2JsonRedisSerializer jsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        // Cache'ten geri okurken somut tipleri çözebilmek için tip bilgisi gömülür.
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        return new GenericJackson2JsonRedisSerializer(objectMapper);
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
                        CacheNames.INDIVIDUAL_CUSTOMERS, baseConfig(Duration.ofMinutes(10)),
                        CacheNames.INDIVIDUAL_CUSTOMER_LIST, baseConfig(Duration.ofMinutes(2))
                ))
                .build();
    }
}
