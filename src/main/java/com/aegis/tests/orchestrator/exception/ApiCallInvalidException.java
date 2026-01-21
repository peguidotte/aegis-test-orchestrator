package com.aegis.tests.orchestrator.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an ApiCall does not belong to the expected TestProject.
 */
public class ApiCallInvalidException extends BusinessException {

    private static final String ERROR_CODE = "API_CALL_INVALID";

    public ApiCallInvalidException(Long apiCallId, Long testProjectId) {
        super(
                String.format("ApiCall with ID %d does not belong to TestProject %d", apiCallId, testProjectId),
                ERROR_CODE,
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }
}
