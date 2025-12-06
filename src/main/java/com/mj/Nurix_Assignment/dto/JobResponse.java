package com.mj.Nurix_Assignment.dto;

import com.mj.Nurix_Assignment.entity.JobStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO containing full job details")
public class JobResponse {

    @Schema(description = "Unique job identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Tenant identifier", example = "tenant-123")
    private String tenantId;

    @Schema(description = "Current job status", example = "PENDING")
    private JobStatus status;

    @Schema(description = "Trace ID for distributed tracing", example = "trace-abc-123")
    private String traceId;

    @Schema(description = "Job creation timestamp")
    private Instant createdAt;

    @Schema(description = "Job start timestamp")
    private Instant startedAt;

    @Schema(description = "Job completion timestamp")
    private Instant completedAt;

    @Schema(description = "Current retry count", example = "0")
    private Integer retryCount;

    @Schema(description = "Maximum retries allowed", example = "3")
    private Integer maxRetries;
}

