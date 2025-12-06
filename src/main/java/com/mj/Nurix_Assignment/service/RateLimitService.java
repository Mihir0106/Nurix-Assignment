package com.mj.Nurix_Assignment.service;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final Cache<String, AtomicInteger> rateLimitCache;

    public boolean allowRequest(String tenantId, int limit) {
        long currentMinute = Instant.now().getEpochSecond() / 60;
        String key = "rate_limit:" + tenantId + ":" + currentMinute;

        AtomicInteger counter = rateLimitCache.get(key, k -> new AtomicInteger(0));

        int currentCount = counter.incrementAndGet();

        if (currentCount > limit) {
            log.warn("Rate limit exceeded for tenant {} (Limit: {}, Count: {})", tenantId, limit, currentCount);
            return false;
        }

        return true;
    }
}
