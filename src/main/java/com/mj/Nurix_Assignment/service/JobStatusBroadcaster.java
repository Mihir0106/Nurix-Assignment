package com.mj.Nurix_Assignment.service;

import com.mj.Nurix_Assignment.dto.JobResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobStatusBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcasts job updates to the tenant-specific topic.
     * Topic format: /topic/jobs/{tenantId}
     *
     * @param job The job details to broadcast
     */
    public void broadcastJobUpdate(JobResponse job) {
        if (job == null || job.getTenantId() == null) {
            log.warn("Cannot broadcast job update: Job or TenantId is null");
            return;
        }

        String destination = "/topic/jobs/" + job.getTenantId();
        try {
            log.debug("Broadcasting update for job {} to {}", job.getId(), destination);
            messagingTemplate.convertAndSend(destination, job);
        } catch (Exception e) {
            log.error("Failed to broadcast job update for job {}", job.getId(), e);
            // We log but don't rethrow to ensure the main transaction/operation doesn't
            // fail
            // due to WebSocket issues.
        }
    }
}
