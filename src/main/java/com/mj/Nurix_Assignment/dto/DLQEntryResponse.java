package com.mj.Nurix_Assignment.dto;

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
@Schema(description = "Response DTO for DLQ entry with original job details")
public class DLQEntryResponse {

    @Schema(description = "DLQ entry identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Original job details")
    private JobResponse job;

    @Schema(description = "Failure reason", example = "Job execution timeout")
    private String failureReason;

    @Schema(description = "Timestamp when job failed")
    private Instant failedAt;

    @Schema(description = "Original payload that failed")
    private String originalPayload;
}

