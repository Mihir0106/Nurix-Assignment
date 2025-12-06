package com.mj.Nurix_Assignment.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, AtomicInteger> rateLimitCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    @Bean
    public Cache<String, Integer> concurrentJobCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.SECONDS)
                .build();
    }
}
