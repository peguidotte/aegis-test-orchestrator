package com.aegis.tests.orchestrator.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an ApiCall is not found.
 */
public class ApiCallNotFoundException extends BusinessException {

    private static final String ERROR_CODE = "API_CALL_NOT_FOUND";

    public ApiCallNotFoundException(Long apiCallId) {
        super(
                String.format("ApiCall with ID %d not found", apiCallId),
                ERROR_CODE,
                HttpStatus.NOT_FOUND
        );
    }
}
