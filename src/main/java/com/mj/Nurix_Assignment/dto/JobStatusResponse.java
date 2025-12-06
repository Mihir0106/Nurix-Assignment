package com.mj.Nurix_Assignment.dto;

import com.mj.Nurix_Assignment.entity.JobStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lightweight response DTO for job status queries")
public class JobStatusResponse {

    @Schema(description = "Unique job identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Current job status", example = "RUNNING")
    private JobStatus status;

    @Schema(description = "Job progress percentage (0-100), if available", example = "75")
    private Integer progress;
}

