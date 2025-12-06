package com.mj.Nurix_Assignment.service;

import com.mj.Nurix_Assignment.entity.Job;
import com.mj.Nurix_Assignment.entity.JobStatus;
import com.mj.Nurix_Assignment.entity.Tenant;
import com.mj.Nurix_Assignment.repository.JobRepository;
import com.mj.Nurix_Assignment.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executor;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobScheduler {

    private final JobRepository jobRepository;
    private final TenantRepository tenantRepository;
    private final JobProcessor jobProcessor;
    private final RetryPolicy retryPolicy;
    private final ConcurrentJobLimiter concurrentJobLimiter;
    private final MetricsService metricsService;
    private final LoggingService loggingService;

    @Qualifier("jobExecutor")
    private final Executor jobExecutor;

    @Scheduled(fixedDelay = 5000)
    public void scheduleJobs() {
        // Register gauge for queue size if needed, or do it in constructor.
        // Better to rely on health check or a separate metrics scheduler, but keeping
        // it simple.

        List<Tenant> tenants = tenantRepository.findAll();

        for (Tenant tenant : tenants) {
            processTenantJobs(tenant);
        }
    }

    private void processTenantJobs(Tenant tenant) {
        if (!concurrentJobLimiter.canLease(tenant.getName(), tenant.getConcurrentJobLimit())) {
            log.trace("Tenant {} reached concurrency limit", tenant.getName());
            return;
        }

        var jobOpt = jobRepository.findFirstByTenantIdAndStatusOrderByCreatedAtAsc(tenant.getName(), JobStatus.PENDING);

        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            leaseAndExecute(job);
        }
    }

    private void leaseAndExecute(Job job) {
        try {
            job.setStatus(JobStatus.RUNNING);
            job.setStartedAt(Timestamp.from(Instant.now()));

            Job leasedJob = jobRepository.save(job);

            loggingService.logJobEvent(com.mj.Nurix_Assignment.enums.JobEvent.JOB_STARTED,
                    leasedJob.getId().toString(), leasedJob.getTenantId(), null, null);

            log.info("Leased job {} for tenant {}", leasedJob.getId(), leasedJob.getTenantId());

            jobExecutor.execute(() -> {
                long startTime = System.currentTimeMillis();
                try {
                    org.slf4j.MDC.put("traceId", leasedJob.getTraceId());
                    org.slf4j.MDC.put("tenantId", leasedJob.getTenantId());
                    org.slf4j.MDC.put("jobId", leasedJob.getId().toString());

                    jobProcessor.process(leasedJob);

                    long duration = System.currentTimeMillis() - startTime;
                    metricsService.recordJobProcessingDuration(leasedJob.getTenantId(), duration);
                    metricsService.incrementJobCompleted(leasedJob.getTenantId(), "SUCCESS");

                    loggingService.logJobEvent(com.mj.Nurix_Assignment.enums.JobEvent.JOB_COMPLETED,
                            leasedJob.getId().toString(), leasedJob.getTenantId(), duration, null);

                    retryPolicy.handleSuccess(leasedJob);
                } catch (Exception e) {
                    long duration = System.currentTimeMillis() - startTime;
                    metricsService.incrementJobFailed(leasedJob.getTenantId(), e.getClass().getSimpleName());

                    loggingService.logJobEvent(com.mj.Nurix_Assignment.enums.JobEvent.JOB_FAILED,
                            leasedJob.getId().toString(), leasedJob.getTenantId(), duration, e.getMessage());

                    retryPolicy.handleFailure(leasedJob, e);
                    // Check if retried
                    if (leasedJob.getStatus() == JobStatus.PENDING) { // Assuming RETRY status exists or similar
                        metricsService.incrementJobRetry(leasedJob.getTenantId());
                        loggingService.logJobEvent(com.mj.Nurix_Assignment.enums.JobEvent.JOB_RETRIED,
                                leasedJob.getId().toString(), leasedJob.getTenantId(), null, null);
                    }
                } finally {
                    org.slf4j.MDC.clear();
                }
            });

        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic lock failure for job {}. It was likely picked up by another instance.", job.getId());
        } catch (Exception e) {
            log.error("Failed to lease job {}", job.getId(), e);
        }
    }
}
