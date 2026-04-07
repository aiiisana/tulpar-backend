package kz.diploma.tulpar.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
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
 * Redis cache configuration with per-cache TTL overrides.
 *
 * Cache names and TTLs:
 *   exercises        — 10 minutes (browsed frequently, changes rarely)
 *   exercise-detail  — 10 minutes
 *   user-progress    — 2 minutes (changes on every submission)
 *
 * CachingConfigurer provides a CacheErrorHandler that catches Redis
 * deserialization errors (e.g. stale entries from old serialization format),
 * evicts the bad key and falls back to the real method call automatically.
 */
@Configuration
@EnableCaching
public class RedisConfig implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Store class type info so deserialisation works without explicit type hints
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(mapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> perCacheConfig = Map.ofEntries(
                Map.entry("exercises",           defaultConfig.entryTtl(Duration.ofMinutes(10))),
                Map.entry("exercise-detail",     defaultConfig.entryTtl(Duration.ofMinutes(10))),
                Map.entry("user-progress",       defaultConfig.entryTtl(Duration.ofMinutes(2))),
                Map.entry("courses",             defaultConfig.entryTtl(Duration.ofMinutes(30))),
                Map.entry("course-levels",       defaultConfig.entryTtl(Duration.ofMinutes(30))),
                Map.entry("flashcards",          defaultConfig.entryTtl(Duration.ofMinutes(60))),
                Map.entry("grammar-rules",       defaultConfig.entryTtl(Duration.ofMinutes(60))),
                Map.entry("grammar-rule-detail", defaultConfig.entryTtl(Duration.ofMinutes(60))),
                Map.entry("article-detail",      defaultConfig.entryTtl(Duration.ofMinutes(30))),
                Map.entry("articles",            defaultConfig.entryTtl(Duration.ofMinutes(15))),
                Map.entry("daily-challenge",     defaultConfig.entryTtl(Duration.ofMinutes(60))),
                Map.entry("user-profile",        defaultConfig.entryTtl(Duration.ofMinutes(10)))
        );

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(perCacheConfig)
                .build();
    }

    /**
     * On any Redis cache READ error (e.g. stale entry with incompatible
     * serialization format), evict the bad key and return null so Spring
     * falls through to the actual method and re-populates the cache cleanly.
     * Write/evict errors are only logged — they are non-fatal.
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException ex, Cache cache, Object key) {
                log.warn("[Cache] Deserialization error in cache='{}' key='{}' — evicting stale entry and falling back to DB. Cause: {}",
                        cache.getName(), key, ex.getMessage());
                try {
                    cache.evict(key);
                } catch (Exception evictEx) {
                    log.warn("[Cache] Could not evict stale key '{}' from cache '{}': {}", key, cache.getName(), evictEx.getMessage());
                }
                // returning normally (not re-throwing) causes Spring to treat this as a cache miss
            }

            @Override
            public void handleCachePutError(RuntimeException ex, Cache cache, Object key, Object value) {
                log.warn("[Cache] Put error in cache='{}' key='{}': {}", cache.getName(), key, ex.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException ex, Cache cache, Object key) {
                log.warn("[Cache] Evict error in cache='{}' key='{}': {}", cache.getName(), key, ex.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException ex, Cache cache) {
                log.warn("[Cache] Clear error in cache='{}': {}", cache.getName(), ex.getMessage());
            }
        };
    }
}
