package com.mj.Nurix_Assignment.service.impl;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Utility component for generating trace IDs.
 * Follows Single Responsibility Principle.
 */
@Component
public class TraceIdGenerator {
    
    public String generate() {
        return "trace-" + UUID.randomUUID().toString().substring(0, 8);
    }
}

