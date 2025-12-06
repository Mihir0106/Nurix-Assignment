package com.mj.Nurix_Assignment.service;

import com.mj.Nurix_Assignment.dto.JobResponse;
import com.mj.Nurix_Assignment.dto.JobStatusResponse;
import com.mj.Nurix_Assignment.dto.JobSubmitRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for job management operations.
 * Follows Single Responsibility Principle (SRP) and Interface Segregation Principle (ISP).
 */
public interface JobService {
    
    /**
     * Submits a new job for processing.
     * 
     * @param request The job submission request
     * @param tenantId The tenant identifier
     * @return JobResponse containing the created job details
     */
    JobResponse submitJob(JobSubmitRequest request, String tenantId);
    
    /**
     * Retrieves job details by ID.
     * 
     * @param jobId The job identifier
     * @param tenantId The tenant identifier for validation
     * @return JobResponse containing job details
     */
    JobResponse getJobById(UUID jobId, String tenantId);
    
    /**
     * Retrieves job status by ID.
     * 
     * @param jobId The job identifier
     * @return JobStatusResponse containing lightweight status information
     */
    JobStatusResponse getJobStatus(UUID jobId);
    
    /**
     * Lists jobs with filtering and pagination.
     * 
     * @param tenantId The tenant identifier (optional filter)
     * @param status The job status (optional filter)
     * @param pageable Pagination parameters
     * @return Page of JobResponse
     */
    Page<JobResponse> listJobs(String tenantId, String status, Pageable pageable);
    
    /**
     * Cancels a pending job.
     * 
     * @param jobId The job identifier
     * @param tenantId The tenant identifier for validation
     */
    void cancelJob(UUID jobId, String tenantId);
}

