package com.mj.Nurix_Assignment.health;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class WorkerHealthIndicator implements HealthIndicator {

    private final ThreadPoolTaskExecutor jobExecutor;

    public WorkerHealthIndicator(@Qualifier("jobExecutor") Executor jobExecutor) {
        if (jobExecutor instanceof ThreadPoolTaskExecutor) {
            this.jobExecutor = (ThreadPoolTaskExecutor) jobExecutor;
        } else {
            this.jobExecutor = null;
        }
    }

    @Override
    public Health health() {
        if (jobExecutor == null) {
            return Health.unknown().withDetail("reason", "Executor is not a ThreadPoolTaskExecutor").build();
        }

        ThreadPoolExecutor threadPool = jobExecutor.getThreadPoolExecutor();

        int activeCount = threadPool.getActiveCount();
        int corePoolSize = threadPool.getCorePoolSize();
        int maxPoolSize = threadPool.getMaximumPoolSize();
        int queueSize = threadPool.getQueue().size();

        return Health.up()
                .withDetail("activeThreads", activeCount)
                .withDetail("corePoolSize", corePoolSize)
                .withDetail("maxPoolSize", maxPoolSize)
                .withDetail("queueSize", queueSize)
                .build();
    }
}
