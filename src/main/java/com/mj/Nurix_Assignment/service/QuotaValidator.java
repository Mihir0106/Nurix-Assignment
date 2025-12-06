package com.mj.Nurix_Assignment.service;

/**
 * Service interface for quota validation.
 * Follows Interface Segregation Principle (ISP) from SOLID.
 */
public interface QuotaValidator {
    
    /**
     * Validates if the tenant has available quota for concurrent jobs.
     * 
     * @param tenantId The tenant identifier
     * @throws QuotaExceededException if quota is exceeded
     */
    void validateQuota(String tenantId);
}

