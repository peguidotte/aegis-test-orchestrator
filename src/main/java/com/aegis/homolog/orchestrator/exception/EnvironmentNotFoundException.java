package com.aegis.homolog.orchestrator.exception;

import org.springframework.http.HttpStatus;

public class EnvironmentNotFoundException extends BusinessException {
    private static final String ERROR_CODE = "ENVIRONMENT_NOT_FOUND";

    public EnvironmentNotFoundException(Long environmentId) {
        super(
                String.format("Environment with ID %d not found", environmentId),
                ERROR_CODE,
                HttpStatus.NOT_FOUND
        );
    }

    public EnvironmentNotFoundException(Long environmentId, Long testProjectId) {
        super(
                String.format("Environment with ID %d not found in TestProject %d", environmentId, testProjectId),
                ERROR_CODE,
                HttpStatus.NOT_FOUND
        );
    }
}

