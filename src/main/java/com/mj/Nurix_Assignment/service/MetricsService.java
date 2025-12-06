package com.mj.Nurix_Assignment.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementJobSubmitted(String tenantId, String status) {
        Counter.builder("jobs.submitted.total")
                .tag("tenant_id", tenantId)
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }

    public void incrementJobCompleted(String tenantId, String result) {
        Counter.builder("jobs.completed.total")
                .tag("tenant_id", tenantId)
                .tag("result", result)
                .register(meterRegistry)
                .increment();
    }

    public void incrementJobFailed(String tenantId, String failureReason) {
        Counter.builder("jobs.failed.total")
                .tag("tenant_id", tenantId)
                .tag("failure_reason", failureReason)
                .register(meterRegistry)
                .increment();
    }

    public void incrementJobRetry(String tenantId) {
        Counter.builder("jobs.retry.count")
                .tag("tenant_id", tenantId)
                .register(meterRegistry)
                .increment();
    }

    public void recordJobProcessingDuration(String tenantId, long durationMs) {
        Timer.builder("job.processing.duration")
                .tag("tenant_id", tenantId)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    // This gauge needs to be registered once typically, or we can use a functional
    // bind.
    // However, since queue size changes dynamic, we typically register it with a
    // state object.
    // If the repository is available, we can bind it in a @PostConstruct or
    // similar.
    // But here I'll provide a method to register it if passed a supplier.
    public void registerQueueSizeGauge(Supplier<Number> queueSizeSupplier) {
        Gauge.builder("jobs.queue.size", queueSizeSupplier)
                .register(meterRegistry);
    }
}
