package com.mj.Nurix_Assignment.controller;

import com.mj.Nurix_Assignment.dto.DLQEntryResponse;
import com.mj.Nurix_Assignment.dto.JobSubmitRequest;
import com.mj.Nurix_Assignment.entity.DLQEntry;
import com.mj.Nurix_Assignment.entity.Job;
import com.mj.Nurix_Assignment.exception.ResourceNotFoundException;
import com.mj.Nurix_Assignment.mapper.JobMapper;
import com.mj.Nurix_Assignment.repository.DLQEntryRepository;
import com.mj.Nurix_Assignment.service.JobService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for Dead Letter Queue (DLQ) operations.
 * Follows RESTful design principles and uses Controller Pattern.
 */
@Slf4j
@RestController
@RequestMapping("/api/dlq")
@RequiredArgsConstructor
@Tag(name = "DLQ", description = "API for managing Dead Letter Queue entries")
public class DLQController {

    private final DLQEntryRepository dlqEntryRepository;
    private final JobService jobService;
    private final JobMapper jobMapper;
    private final ObjectMapper objectMapper;

    @GetMapping
    @Operation(summary = "List DLQ entries", description = "Retrieves all DLQ entries, optionally filtered by tenant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DLQ entries retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<DLQEntryResponse>> listDLQEntries(
            @Parameter(description = "Filter by tenant ID")
            @RequestParam(required = false) String tenantId) {
        
        log.debug("Listing DLQ entries for tenant: {}", tenantId);
        
        List<DLQEntry> entries;
        if (tenantId != null) {
            entries = dlqEntryRepository.findAll().stream()
                    .filter(entry -> entry.getJob() != null && 
                            entry.getJob().getTenantId().equals(tenantId))
                    .collect(Collectors.toList());
        } else {
            entries = dlqEntryRepository.findAll();
        }

        List<DLQEntryResponse> responses = entries.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "Retry a DLQ job", description = "Creates a new job from a DLQ entry for retry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Job retried successfully",
                    content = @Content(schema = @Schema(implementation = com.mj.Nurix_Assignment.dto.JobResponse.class))),
            @ApiResponse(responseCode = "404", description = "DLQ entry not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<com.mj.Nurix_Assignment.dto.JobResponse> retryDLQJob(
            @Parameter(description = "DLQ entry identifier", required = true)
            @PathVariable UUID id) {
        
        log.info("Retrying DLQ entry: {}", id);
        
        DLQEntry dlqEntry = dlqEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DLQ entry not found: " + id));

        Job originalJob = dlqEntry.getJob();
        if (originalJob == null) {
            throw new ResourceNotFoundException("Original job not found for DLQ entry: " + id);
        }

        // Parse original payload
        Map<String, Object> payload;
        try {
            payload = objectMapper.readValue(
                    dlqEntry.getOriginalPayload() != null ? 
                            dlqEntry.getOriginalPayload() : originalJob.getPayload(),
                    new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid payload format in DLQ entry", e);
        }

        // Create new job submission request
        JobSubmitRequest retryRequest = JobSubmitRequest.builder()
                .payload(payload)
                .maxRetries(originalJob.getMaxRetries())
                .build();

        // Submit the retry job
        com.mj.Nurix_Assignment.dto.JobResponse response = jobService.submitJob(
                retryRequest, originalJob.getTenantId());

        log.info("DLQ entry {} retried successfully, new job: {}", id, response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private DLQEntryResponse toResponse(DLQEntry entry) {
        return DLQEntryResponse.builder()
                .id(entry.getId())
                .job(entry.getJob() != null ? jobMapper.toResponse(entry.getJob()) : null)
                .failureReason(entry.getFailureReason())
                .failedAt(entry.getFailedAt() != null ? entry.getFailedAt().toInstant() : null)
                .originalPayload(entry.getOriginalPayload())
                .build();
    }
}

