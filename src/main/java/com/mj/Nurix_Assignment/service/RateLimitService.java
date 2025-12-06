package com.mj.Nurix_Assignment.service;

/**
 * Service interface for rate limiting validation.
 * Follows Interface Segregation Principle (ISP) from SOLID.
 */
public interface RateLimitService {
    
    /**
     * Validates if the tenant has exceeded their rate limit.
     * 
     * @param tenantId The tenant identifier
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    void validateRateLimit(String tenantId);
}

