package com.mj.Nurix_Assignment.health;

import com.mj.Nurix_Assignment.entity.JobStatus;
import com.mj.Nurix_Assignment.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueHealthIndicator implements HealthIndicator {

    private final JobRepository jobRepository;
    private static final long THRESHOLD = 1000;

    @Override
    public Health health() {
        long pendingCount = jobRepository.countByStatus(JobStatus.PENDING);

        Health.Builder builder = pendingCount > THRESHOLD
                ? Health.status("WARN") // Make sure to map WARN in application properties if needed, default allows
                                        // custom status
                : Health.up();

        return builder
                .withDetail("queueDepth", pendingCount)
                .withDetail("threshold", THRESHOLD)
                .build();
    }
}
