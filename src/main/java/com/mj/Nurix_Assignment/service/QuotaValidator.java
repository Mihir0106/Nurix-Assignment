package com.mj.Nurix_Assignment.service;

import com.mj.Nurix_Assignment.dto.ValidationResult;
import com.mj.Nurix_Assignment.entity.Tenant;
import com.mj.Nurix_Assignment.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuotaValidator {

    private final RateLimitService rateLimitService;
    private final ConcurrentJobLimiter concurrentJobLimiter;
    private final TenantRepository tenantRepository;

    public ValidationResult validate(String tenantId) {
        Optional<Tenant> tenantOpt = tenantRepository.findByName(tenantId);

        if (tenantOpt.isEmpty()) {
            return ValidationResult.failure("TENANT_NOT_FOUND", "Tenant not found");
        }

        Tenant tenant = tenantOpt.get();

        // 1. Check Rate Limit
        if (!rateLimitService.allowRequest(tenantId, tenant.getRateLimit())) {
            return ValidationResult.failure("RATE_LIMIT_EXCEEDED", "Rate limit exceeded for tenant");
        }

        // 2. Check Concurrent Job Limit
        // Note: This check is usually done at "submit" time.
        // If checking at "lease" time, the logic is slightly different (we don't
        // increment, just check).
        // Based on prompt "validate(String tenantId, JobSubmitRequest request)", this
        // is likely for submission.

        if (!concurrentJobLimiter.canLease(tenantId, tenant.getConcurrentJobLimit())) {
            return ValidationResult.failure("CONCURRENT_LIMIT_EXCEEDED", "Concurrent job limit reached");
        }

        return ValidationResult.success();
    }
}
