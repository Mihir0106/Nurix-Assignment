package com.mj.Nurix_Assignment.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.mj.Nurix_Assignment.entity.JobStatus;
import com.mj.Nurix_Assignment.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConcurrentJobLimiter {

    private final JobRepository jobRepository;
    private final Cache<String, Integer> concurrentJobCache;

    public boolean canLease(String tenantId, int limit) {
        // We use cache to avoid hitting DB for every request
        // Key is just tenantId
        Integer currentRunningJobs = concurrentJobCache.get(tenantId, key -> {
            // Fallback to DB query
            return (int) jobRepository.countByTenantIdAndStatus(tenantId, JobStatus.RUNNING);
        });

        if (currentRunningJobs >= limit) {
            log.warn("Concurrent job limit reached for tenant {} (Limit: {}, Running: {})", tenantId, limit,
                    currentRunningJobs);
            return false;
        }

        return true;
    }

    // Helper to invalidate cache when a job starts/ends, ensuring near real-time
    // accuracy if needed
    // However, for high scale, we might accept 5s staleness as per requirement.
    // The requirement says "Cache results for 5 seconds", implying we rely on cache
    // expiry.
}
