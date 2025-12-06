package com.mj.Nurix_Assignment.controller;

import com.mj.Nurix_Assignment.dto.JobResponse;
import com.mj.Nurix_Assignment.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final JobService jobService;

    /**
     * Handle subscriptions to /app/jobs/{tenantId}.
     * Returns the current list of jobs so the client acts "immediately" upon
     * subscription.
     * Note: This maps to the application prefix /app, so client subscribes to
     * /app/jobs/{tenantId}
     * for the initial state, and also listens to /topic/jobs/{tenantId} for
     * updates.
     * Or, simpler: client subscribes to /topic/jobs/{tenantId}.
     *
     * If we use @SubscribeMapping, it intercepts the subscription to the broker.
     */
    @SubscribeMapping("/jobs/{tenantId}")
    public List<JobResponse> subscribeToJobs(@DestinationVariable String tenantId) {
        log.info("New subscription for tenant: {}", tenantId);
        // Return latest 20 jobs as initial state
        return jobService.listJobs(tenantId, null,
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
    }
}
