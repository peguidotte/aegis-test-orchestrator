package com.aegis.tests.orchestrator.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when method and path are required but not provided in MANUAL mode.
 */
public class ManualInputRequiredException extends BusinessException {

    private static final String ERROR_CODE = "MANUAL_INPUT_REQUIRED";

    public ManualInputRequiredException() {
        super(
                "Method and path are required when inputType is MANUAL",
                ERROR_CODE,
                HttpStatus.BAD_REQUEST
        );
    }
}
