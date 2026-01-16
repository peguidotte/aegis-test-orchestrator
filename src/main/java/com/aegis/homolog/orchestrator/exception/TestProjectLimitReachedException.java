package com.aegis.homolog.orchestrator.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when the TestProject limit per project is reached.
 */
public class TestProjectLimitReachedException extends BusinessException {

    private static final String ERROR_CODE = "TEST_PROJECT_LIMIT_REACHED";

    public TestProjectLimitReachedException(Long projectId) {
        super(
                String.format("Project %d has already reached the maximum limit of TestProjects", projectId),
                ERROR_CODE,
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }
}

