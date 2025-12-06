package com.mj.Nurix_Assignment.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setup() {
        Cache<String, AtomicInteger> cache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
        rateLimitService = new RateLimitService(cache);
    }

    @Test
    void testAllowRequest() {
        String tenantId = "test-tenant";
        int limit = 5;

        // Make 5 allowed requests
        for (int i = 0; i < 5; i++) {
            assertThat(rateLimitService.allowRequest(tenantId, limit)).isTrue();
        }

        // 6th request should fail
        assertThat(rateLimitService.allowRequest(tenantId, limit)).isFalse();
    }
}
