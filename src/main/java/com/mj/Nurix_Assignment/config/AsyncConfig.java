package com.mj.Nurix_Assignment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
// Scheduling config moved to SchedulingConfig.java to allow disabling
// scheduling while keeping executor
public class AsyncConfig {

    @Bean(name = "jobExecutor")
    public Executor jobExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Worker-");
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
        return executor;
    }

    static class MdcTaskDecorator implements org.springframework.core.task.TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            java.util.Map<String, String> contextMap = org.slf4j.MDC.getCopyOfContextMap();
            return () -> {
                try {
                    if (contextMap != null) {
                        org.slf4j.MDC.setContextMap(contextMap);
                    }
                    runnable.run();
                } finally {
                    org.slf4j.MDC.clear();
                }
            };
        }
    }
}
