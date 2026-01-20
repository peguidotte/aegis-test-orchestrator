package com.aegis.homolog.orchestrator.exception;

import org.springframework.http.HttpStatus;

public class TestProjectNotFoundException extends BusinessException {
    private static final String ERROR_CODE = "TEST_PROJECT_NOT_FOUND";

    public TestProjectNotFoundException(Long testProjectId) {
        super(
                String.format("TestProject with ID %d not found", testProjectId),
                ERROR_CODE,
                HttpStatus.NOT_FOUND
        );
    }
}

