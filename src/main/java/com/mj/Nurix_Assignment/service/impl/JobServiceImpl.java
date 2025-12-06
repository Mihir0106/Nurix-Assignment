package com.mj.Nurix_Assignment.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mj.Nurix_Assignment.dto.JobResponse;
import com.mj.Nurix_Assignment.dto.JobStatusResponse;
import com.mj.Nurix_Assignment.dto.JobSubmitRequest;
import com.mj.Nurix_Assignment.entity.Job;
import com.mj.Nurix_Assignment.entity.JobStatus;
import com.mj.Nurix_Assignment.exception.ResourceNotFoundException;
import com.mj.Nurix_Assignment.mapper.JobMapper;
import com.mj.Nurix_Assignment.repository.JobRepository;
import com.mj.Nurix_Assignment.service.JobService;
import com.mj.Nurix_Assignment.service.QuotaValidator;
import com.mj.Nurix_Assignment.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of job service.
 * Uses Service Layer Pattern and follows Single Responsibility Principle.
 * Delegates to specialized services (RateLimitService, QuotaValidator) following Dependency Inversion Principle.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final RateLimitService rateLimitService;
    private final QuotaValidator quotaValidator;
    private final JobMapper jobMapper;
    private final ObjectMapper objectMapper;
    private final TraceIdGenerator traceIdGenerator;

    @Override
    @Transactional
    public JobResponse submitJob(JobSubmitRequest request, String tenantId) {
        log.info("Submitting job for tenant: {}", tenantId);

        // Check idempotency
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            var existingJob = jobRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existingJob.isPresent()) {
                log.info("Idempotent request detected, returning existing job: {}", existingJob.get().getId());
                return jobMapper.toResponse(existingJob.get());
            }
        }

        // Validate rate limits
        rateLimitService.validateRateLimit(tenantId);

        // Validate quota
        quotaValidator.validateQuota(tenantId);

        // Create new job
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(request.getPayload());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid payload format", e);
        }

        String traceId = traceIdGenerator.generate();

        Job job = Job.builder()
                .tenantId(tenantId)
                .payload(payloadJson)
                .status(JobStatus.PENDING)
                .idempotencyKey(request.getIdempotencyKey())
                .maxRetries(request.getMaxRetries() != null ? request.getMaxRetries() : 3)
                .retryCount(0)
                .traceId(traceId)
                .build();

        Job savedJob = jobRepository.save(job);
        log.info("Job created successfully: {} for tenant: {}", savedJob.getId(), tenantId);

        return jobMapper.toResponse(savedJob);
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getJobById(UUID jobId, String tenantId) {
        log.debug("Fetching job: {} for tenant: {}", jobId, tenantId);
        
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));

        // Validate tenant
        if (!job.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Job not found: " + jobId);
        }

        return jobMapper.toResponse(job);
    }

    @Override
    @Transactional(readOnly = true)
    public JobStatusResponse getJobStatus(UUID jobId) {
        log.debug("Fetching job status: {}", jobId);
        
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));

        return jobMapper.toStatusResponse(job);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobResponse> listJobs(String tenantId, String status, Pageable pageable) {
        log.debug("Listing jobs for tenant: {}, status: {}", tenantId, status);

        Page<Job> jobs;
        
        if (tenantId != null && status != null) {
            JobStatus jobStatus = JobStatus.valueOf(status.toUpperCase());
            jobs = jobRepository.findByTenantIdAndStatus(tenantId, jobStatus, pageable);
        } else if (tenantId != null) {
            jobs = jobRepository.findByTenantId(tenantId, pageable);
        } else if (status != null) {
            JobStatus jobStatus = JobStatus.valueOf(status.toUpperCase());
            jobs = jobRepository.findByStatus(jobStatus, pageable);
        } else {
            jobs = jobRepository.findAll(pageable);
        }

        return jobs.map(jobMapper::toResponse);
    }

    @Override
    @Transactional
    public void cancelJob(UUID jobId, String tenantId) {
        log.info("Cancelling job: {} for tenant: {}", jobId, tenantId);
        
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));

        // Validate tenant
        if (!job.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Job not found: " + jobId);
        }

        // Only cancel pending jobs
        if (job.getStatus() != JobStatus.PENDING) {
            throw new IllegalStateException("Only pending jobs can be cancelled");
        }

        job.setStatus(JobStatus.CANCELLED);
        jobRepository.save(job);
        
        log.info("Job cancelled successfully: {}", jobId);
    }
}

