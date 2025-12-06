package com.mj.Nurix_Assignment.controller;

import com.mj.Nurix_Assignment.dto.JobResponse;
import com.mj.Nurix_Assignment.dto.JobStatusResponse;
import com.mj.Nurix_Assignment.dto.JobSubmitRequest;
import com.mj.Nurix_Assignment.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for job management operations.
 * Follows RESTful design principles and uses Controller Pattern.
 */
@Slf4j
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "API for managing jobs in the distributed task queue system")
public class JobController {

    private final JobService jobService;

    @PostMapping
    @Operation(summary = "Submit a new job", description = "Creates a new job with validation, rate limiting, and idempotency checks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Job created successfully",
                    content = @Content(schema = @Schema(implementation = JobResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<JobResponse> submitJob(
            @Valid @RequestBody JobSubmitRequest request,
            @Parameter(description = "Tenant identifier", required = true)
            @RequestHeader("X-Tenant-Id") String tenantId) {
        
        log.info("Received job submission request for tenant: {}", tenantId);
        JobResponse response = jobService.submitJob(request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job by ID", description = "Retrieves detailed information about a specific job")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job found",
                    content = @Content(schema = @Schema(implementation = JobResponse.class))),
            @ApiResponse(responseCode = "404", description = "Job not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<JobResponse> getJob(
            @Parameter(description = "Job identifier", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Tenant identifier", required = true)
            @RequestHeader("X-Tenant-Id") String tenantId) {
        
        log.debug("Fetching job: {} for tenant: {}", id, tenantId);
        JobResponse response = jobService.getJobById(id, tenantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Get job status", description = "Retrieves lightweight status information about a specific job")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job status found",
                    content = @Content(schema = @Schema(implementation = JobStatusResponse.class))),
            @ApiResponse(responseCode = "404", description = "Job not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<JobStatusResponse> getJobStatus(
            @Parameter(description = "Job identifier", required = true)
            @PathVariable UUID id) {
        
        log.debug("Fetching job status: {}", id);
        JobStatusResponse response = jobService.getJobStatus(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List jobs", description = "Retrieves a paginated list of jobs with optional filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Jobs retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<JobResponse>> listJobs(
            @Parameter(description = "Filter by tenant ID")
            @RequestParam(required = false) String tenantId,
            @Parameter(description = "Filter by job status (PENDING, RUNNING, COMPLETED, FAILED, CANCELLED)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Listing jobs - tenantId: {}, status: {}, page: {}, size: {}", 
                tenantId, status, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<JobResponse> jobs = jobService.listJobs(tenantId, status, pageable);
        return ResponseEntity.ok(jobs);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a pending job", description = "Cancels a job that is in PENDING status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Job cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Job cannot be cancelled"),
            @ApiResponse(responseCode = "404", description = "Job not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> cancelJob(
            @Parameter(description = "Job identifier", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Tenant identifier", required = true)
            @RequestHeader("X-Tenant-Id") String tenantId) {
        
        log.info("Cancelling job: {} for tenant: {}", id, tenantId);
        jobService.cancelJob(id, tenantId);
        return ResponseEntity.noContent().build();
    }
}

