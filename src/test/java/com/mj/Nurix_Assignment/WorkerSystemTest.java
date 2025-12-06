package com.mj.Nurix_Assignment;

import com.mj.Nurix_Assignment.entity.Job;
import com.mj.Nurix_Assignment.entity.JobStatus;
import com.mj.Nurix_Assignment.entity.Tenant;
import com.mj.Nurix_Assignment.repository.JobRepository;
import com.mj.Nurix_Assignment.repository.TenantRepository;
import com.mj.Nurix_Assignment.service.JobScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
public class WorkerSystemTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private JobScheduler jobScheduler;

    @BeforeEach
    void setup() {
        jobRepository.deleteAll();
        tenantRepository.deleteAll();

        Tenant tenant = Tenant.builder()
                .name("test-tenant")
                .concurrentJobLimit(5)
                .rateLimit(100)
                .build();
        tenantRepository.save(tenant);
    }

    @Test
    void testJobLeasingAndExecution() {
        // Create a PENDING job
        Job job = Job.builder()
                .tenantId("test-tenant")
                .payload("{\"test\":\"data\"}")
                .status(JobStatus.PENDING)
                .idempotencyKey(UUID.randomUUID().toString())
                .build();
        jobRepository.save(job);

        // Trigger Scheduler Manually (or wait for @Scheduled if enabled in test)
        // Since @Scheduled catches up, we can also just call the method directly to be
        // deterministic
        jobScheduler.scheduleJobs();

        // Verify Job transitions to RUNNING -> COMPLETED
        // Since execution is async, we await
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            Job updatedJob = jobRepository.findById(job.getId()).orElseThrow();
            assertThat(updatedJob.getStatus()).isEqualTo(JobStatus.COMPLETED);
        });
    }

    @Test
    void testConcurrencyLimit() {
        // Create 5 RUNNING jobs (limit is 5)
        for (int i = 0; i < 5; i++) {
            Job job = Job.builder()
                    .tenantId("test-tenant")
                    .payload("{}")
                    .status(JobStatus.RUNNING)
                    .idempotencyKey("running-" + i)
                    .build();
            jobRepository.save(job);
        }

        // Create 1 PENDING job (should NOT be picked up)
        Job pendingJob = Job.builder()
                .tenantId("test-tenant")
                .payload("{}")
                .status(JobStatus.PENDING)
                .idempotencyKey("pending-1")
                .build();
        jobRepository.save(pendingJob);

        // Trigger Scheduler
        jobScheduler.scheduleJobs();

        // Verify PENDING job is still PENDING
        Job checkJob = jobRepository.findById(pendingJob.getId()).orElseThrow();
        assertThat(checkJob.getStatus()).isEqualTo(JobStatus.PENDING);
    }
}
