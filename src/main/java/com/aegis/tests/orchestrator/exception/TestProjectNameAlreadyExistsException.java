package com.aegis.tests.orchestrator.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a TestProject with the same name already exists in the project.
 */
public class TestProjectNameAlreadyExistsException extends BusinessException {

    private static final String ERROR_CODE = "TEST_PROJECT_NAME_ALREADY_EXISTS";

    public TestProjectNameAlreadyExistsException(String name, Long projectId) {
        super(
                String.format("A TestProject with name '%s' already exists in project %d", name, projectId),
                ERROR_CODE,
                HttpStatus.CONFLICT,
                "name"
        );
    }
}

