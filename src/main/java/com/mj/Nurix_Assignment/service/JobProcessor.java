package com.mj.Nurix_Assignment.service;

import com.mj.Nurix_Assignment.entity.Job;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class JobProcessor {

    public void process(Job job) throws InterruptedException {
        try {
            if (job.getTraceId() != null) {
                MDC.put("traceId", job.getTraceId());
            }

            log.info("Processing job: {}", job.getId());

            // Parse payload (Mock)
            log.debug("Payload: {}", job.getPayload());

            // Simulate work
            int sleepTime = ThreadLocalRandom.current().nextInt(5000, 10001);
            Thread.sleep(sleepTime);

            log.info("Job {} completed successfully in {} ms", job.getId(), sleepTime);
        } finally {
            MDC.clear();
        }
    }
}
