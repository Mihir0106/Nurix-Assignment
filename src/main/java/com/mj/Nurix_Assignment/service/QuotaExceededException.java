package com.mj.Nurix_Assignment.service;

/**
 * Exception thrown when quota is exceeded.
 */
public class QuotaExceededException extends RuntimeException {
    
    public QuotaExceededException(String message) {
        super(message);
    }
}

