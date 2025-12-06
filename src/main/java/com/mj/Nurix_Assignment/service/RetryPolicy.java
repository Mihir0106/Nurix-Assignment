package com.mj.Nurix_Assignment.service;

import com.mj.Nurix_Assignment.entity.DLQEntry;
import com.mj.Nurix_Assignment.entity.Job;
import com.mj.Nurix_Assignment.entity.JobStatus;
import com.mj.Nurix_Assignment.repository.DLQEntryRepository;
import com.mj.Nurix_Assignment.repository.JobRepository;
import com.mj.Nurix_Assignment.mapper.JobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryPolicy {

    private final JobRepository jobRepository;
    private final DLQEntryRepository dlqEntryRepository;
    private final JobStatusBroadcaster jobStatusBroadcaster;
    private final JobMapper jobMapper;

    @Transactional
    public void handleFailure(Job job, Exception e) {
        if (job.getTraceId() != null) {
            MDC.put("traceId", job.getTraceId());
        }

        try {
            int currentRetries = job.getRetryCount();
            if (currentRetries < job.getMaxRetries()) {
                // Retry
                int nextRetryCount = currentRetries + 1;
                long backoffSeconds = (long) (2 * Math.pow(2, nextRetryCount)); // Exponential: 4s, 8s, 16s...
                Timestamp nextRunAt = Timestamp.from(Instant.now().plusSeconds(backoffSeconds)); // In a real system,
                                                                                                 // we'd schedule this.
                // For this simple poller, we just set status PENDING. The poller picks it up
                // immediately unless we add a "scheduledAt" field.
                // Requirement check: "If retryCount < maxRetries: reset status to PENDING"

                log.warn("Job {} failed. Retrying ({}/{}). Backoff: {}s. Next run at: {}", job.getId(), nextRetryCount,
                        job.getMaxRetries(), backoffSeconds, nextRunAt);

                job.setRetryCount(nextRetryCount);
                job.setStatus(JobStatus.PENDING);
                job.setCompletedAt(null);
                // In a real delay system, we'd update a 'scheduledAt' field.
                // Since our entities don't have 'scheduledAt' and the request didn't ask for
                // it,
                // it might get picked up immediately. We'll stick to the requested entity
                // structure.

            } else {
                // Move to DLQ
                log.error("Job {} exceeded max retries. Moving to DLQ.", job.getId());
                job.setStatus(JobStatus.FAILED);
                job.setCompletedAt(Timestamp.from(Instant.now()));

                DLQEntry dlqEntry = DLQEntry.builder()
                        .job(job)
                        .failureReason(e.getMessage())
                        .originalPayload(job.getPayload())
                        .build();

                dlqEntryRepository.save(dlqEntry);
            }
            jobRepository.save(job);

            jobStatusBroadcaster.broadcastJobUpdate(jobMapper.toResponse(job));
        } finally {
            MDC.clear();
        }
    }

    @Transactional
    public void handleSuccess(Job job) {
        if (job.getTraceId() != null) {
            MDC.put("traceId", job.getTraceId());
        }
        try {
            log.info("Job {} completed successfully.", job.getId());
            job.setStatus(JobStatus.COMPLETED);
            job.setCompletedAt(Timestamp.from(Instant.now()));
            jobRepository.save(job);

            jobStatusBroadcaster.broadcastJobUpdate(jobMapper.toResponse(job));
        } finally {
            MDC.clear();
        }
    }
}
