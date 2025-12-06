package com.mj.Nurix_Assignment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mj.Nurix_Assignment.enums.JobEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoggingService {

    private final ObjectMapper objectMapper;

    public void logJobEvent(JobEvent event, String jobId, String tenantId, Long durationMs, Object payload) {
        Map<String, Object> logMap = new LinkedHashMap<>();
        logMap.put("event", event);
        logMap.put("jobId", jobId);
        logMap.put("tenantId", tenantId);
        if (durationMs != null) {
            logMap.put("duration", durationMs);
        }
        if (payload != null) {
            logMap.put("payload", payload);
        }

        // Add traceId from MDC if available
        String traceId = MDC.get("traceId");
        if (traceId != null) {
            logMap.put("traceId", traceId);
        }

        try {
            // We use INFO level for job events
            log.info(objectMapper.writeValueAsString(logMap));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize job event log", e);
        }
    }
}
