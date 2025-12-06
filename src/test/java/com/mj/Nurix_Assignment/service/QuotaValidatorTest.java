package com.mj.Nurix_Assignment.service;

import com.mj.Nurix_Assignment.dto.ValidationResult;
import com.mj.Nurix_Assignment.entity.Tenant;
import com.mj.Nurix_Assignment.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuotaValidatorTest {

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private ConcurrentJobLimiter concurrentJobLimiter;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private QuotaValidator quotaValidator;

    private Tenant tenant;

    @BeforeEach
    void setup() {
        tenant = Tenant.builder()
                .name("test-tenant")
                .rateLimit(10)
                .concurrentJobLimit(5)
                .build();
    }

    @Test
    void testValidate_Success() {
        when(tenantRepository.findByName("test-tenant")).thenReturn(Optional.of(tenant));
        when(rateLimitService.allowRequest(eq("test-tenant"), anyInt())).thenReturn(true);
        when(concurrentJobLimiter.canLease(eq("test-tenant"), anyInt())).thenReturn(true);

        ValidationResult result = quotaValidator.validate("test-tenant");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void testValidate_TenantNotFound() {
        when(tenantRepository.findByName("unknown")).thenReturn(Optional.empty());

        ValidationResult result = quotaValidator.validate("unknown");
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("TENANT_NOT_FOUND");
    }

    @Test
    void testValidate_RateLimitExceeded() {
        when(tenantRepository.findByName("test-tenant")).thenReturn(Optional.of(tenant));
        when(rateLimitService.allowRequest(eq("test-tenant"), anyInt())).thenReturn(false);

        ValidationResult result = quotaValidator.validate("test-tenant");
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("RATE_LIMIT_EXCEEDED");
    }

    @Test
    void testValidate_ConcurrentLimitExceeded() {
        when(tenantRepository.findByName("test-tenant")).thenReturn(Optional.of(tenant));
        when(rateLimitService.allowRequest(eq("test-tenant"), anyInt())).thenReturn(true);
        when(concurrentJobLimiter.canLease(eq("test-tenant"), anyInt())).thenReturn(false);

        ValidationResult result = quotaValidator.validate("test-tenant");
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("CONCURRENT_LIMIT_EXCEEDED");
    }
}
