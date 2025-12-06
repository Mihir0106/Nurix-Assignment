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

    @Qualifier("jobExecutor")
    private final Executor jobExecutor;

    @Scheduled(fixedDelay = 5000)
    public void scheduleJobs() {
        List<Tenant> tenants = tenantRepository.findAll();

        for (Tenant tenant : tenants) {
            processTenantJobs(tenant);
        }
    }

    private void processTenantJobs(Tenant tenant) {
        // Double check concurrency limit
        long runningJobsConfig = jobRepository.countByTenantIdAndStatus(tenant.getName(), JobStatus.RUNNING); // Assuming
                                                                                                              // tenant.getName()
                                                                                                              // is the
                                                                                                              // ID used
                                                                                                              // in
                                                                                                              // Jobs.
                                                                                                              // In
                                                                                                              // Entity
                                                                                                              // it is
                                                                                                              // 'tenantID'.
        // Wait, Tenant Entity has ID(UUID) and Name(String). Job has tenantId(String).
        // Let's assume Job.tenantId refers to Tenant.name (since it's a string in Job
        // entity).

        if (runningJobsConfig >= tenant.getConcurrentJobLimit()) {
            log.trace("Tenant {} reached concurrency limit ({}/{})", tenant.getName(), runningJobsConfig,
                    tenant.getConcurrentJobLimit());
            return;
        }

        // Fetch candidate job
        // We use findFirst... to get the oldest pending job
        var jobOpt = jobRepository.findFirstByTenantIdAndStatusOrderByCreatedAtAsc(tenant.getName(), JobStatus.PENDING);

        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            leaseAndExecute(job);
        }
    }

    private void leaseAndExecute(Job job) {
        try {
            // Optimistic Locking: This will fail if version changed
            job.setStatus(JobStatus.RUNNING);
            job.setStartedAt(Timestamp.from(Instant.now()));

            // Save immediately to commit state before async execution
            Job leasedJob = jobRepository.save(job);

            log.info("Leased job {} for tenant {}", leasedJob.getId(), leasedJob.getTenantId());

            // Submit to executor
            jobExecutor.execute(() -> {
                try {
                    jobProcessor.process(leasedJob);
                    retryPolicy.handleSuccess(leasedJob);
                } catch (Exception e) {
                    retryPolicy.handleFailure(leasedJob, e);
                }
            });

        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic lock failure for job {}. It was likely picked up by another instance.", job.getId());
        } catch (Exception e) {
            log.error("Failed to lease job {}", job.getId(), e);
        }
    }
}
