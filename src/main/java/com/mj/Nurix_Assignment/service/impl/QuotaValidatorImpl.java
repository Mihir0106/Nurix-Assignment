package com.mj.Nurix_Assignment.service.impl;

import com.mj.Nurix_Assignment.entity.JobStatus;
import com.mj.Nurix_Assignment.entity.Tenant;
import com.mj.Nurix_Assignment.repository.JobRepository;
import com.mj.Nurix_Assignment.repository.TenantRepository;
import com.mj.Nurix_Assignment.service.QuotaExceededException;
import com.mj.Nurix_Assignment.service.QuotaValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of quota validation service.
 * Uses Strategy Pattern for quota validation logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaValidatorImpl implements QuotaValidator {

    private final TenantRepository tenantRepository;
    private final JobRepository jobRepository;

    @Override
    @Transactional(readOnly = true)
    public void validateQuota(String tenantId) {
        Tenant tenant = tenantRepository.findByName(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        long runningJobsCount = jobRepository.countByTenantIdAndStatus(
                tenantId, JobStatus.RUNNING);

        if (runningJobsCount >= tenant.getConcurrentJobLimit()) {
            log.warn("Quota exceeded for tenant: {} (limit: {}, current: {})", 
                    tenantId, tenant.getConcurrentJobLimit(), runningJobsCount);
            throw new QuotaExceededException(
                    String.format("Concurrent job limit exceeded. Maximum %d concurrent jobs allowed.", 
                            tenant.getConcurrentJobLimit()));
        }

        log.debug("Quota check passed for tenant: {} (current: {}/{})", 
                tenantId, runningJobsCount, tenant.getConcurrentJobLimit());
    }
}

