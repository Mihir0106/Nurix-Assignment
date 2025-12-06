package com.mj.Nurix_Assignment.service.impl;

import com.mj.Nurix_Assignment.entity.Tenant;
import com.mj.Nurix_Assignment.repository.JobRepository;
import com.mj.Nurix_Assignment.repository.TenantRepository;
import com.mj.Nurix_Assignment.service.RateLimitExceededException;
import com.mj.Nurix_Assignment.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Implementation of rate limiting service.
 * Uses Strategy Pattern for rate limiting logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private final TenantRepository tenantRepository;
    private final JobRepository jobRepository;

    @Override
    @Transactional(readOnly = true)
    public void validateRateLimit(String tenantId) {
        Tenant tenant = tenantRepository.findByName(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        // Count jobs created in the last minute (rate limit window)
        // Note: This is a simplified implementation. For production, consider using Redis or a more efficient approach
        java.sql.Timestamp oneMinuteAgo = java.sql.Timestamp.from(
                Instant.now().minus(1, ChronoUnit.MINUTES));
        
        long recentJobCount = jobRepository.findAll().stream()
                .filter(job -> job.getTenantId().equals(tenantId))
                .filter(job -> job.getCreatedAt() != null && 
                        job.getCreatedAt().after(oneMinuteAgo))
                .count();

        if (recentJobCount >= tenant.getRateLimit()) {
            log.warn("Rate limit exceeded for tenant: {} (limit: {}, current: {})", 
                    tenantId, tenant.getRateLimit(), recentJobCount);
            throw new RateLimitExceededException(
                    String.format("Rate limit exceeded. Maximum %d jobs per minute allowed.", 
                            tenant.getRateLimit()));
        }

        log.debug("Rate limit check passed for tenant: {} (current: {}/{})", 
                tenantId, recentJobCount, tenant.getRateLimit());
    }
}

