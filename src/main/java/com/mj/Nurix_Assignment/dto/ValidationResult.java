package com.mj.Nurix_Assignment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidationResult {
    private boolean isValid;
    private String errorMessage;
    private String errorCode;

    public static ValidationResult success() {
        return ValidationResult.builder().isValid(true).build();
    }

    public static ValidationResult failure(String errorCode, String errorMessage) {
        return ValidationResult.builder()
                .isValid(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
}
