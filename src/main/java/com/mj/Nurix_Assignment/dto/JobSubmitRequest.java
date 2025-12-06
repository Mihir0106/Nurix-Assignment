package com.mj.Nurix_Assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for submitting a new job")
public class JobSubmitRequest {

    @NotNull(message = "Payload is required")
    @Schema(description = "Job payload as JSON object", example = "{\"key\": \"value\"}")
    private Map<String, Object> payload;

    @Schema(description = "Idempotency key for ensuring duplicate requests return the same job", example = "unique-key-123")
    private String idempotencyKey;

    @Min(value = 0, message = "Max retries must be non-negative")
    @Builder.Default
    @Schema(description = "Maximum number of retries for this job", example = "3", defaultValue = "3")
    private Integer maxRetries = 3;
}

