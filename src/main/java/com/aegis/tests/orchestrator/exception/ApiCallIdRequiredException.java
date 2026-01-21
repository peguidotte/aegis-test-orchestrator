package com.aegis.tests.orchestrator.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when apiCallId is required but not provided in API_CALL mode.
 */
public class ApiCallIdRequiredException extends BusinessException {

    private static final String ERROR_CODE = "API_CALL_ID_REQUIRED";

    public ApiCallIdRequiredException() {
        super(
                "ApiCallId is required when inputType is API_CALL",
                ERROR_CODE,
                HttpStatus.BAD_REQUEST
        );
    }
}
