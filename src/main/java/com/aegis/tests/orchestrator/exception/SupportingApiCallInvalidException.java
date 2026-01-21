package com.aegis.tests.orchestrator.exception;

import org.springframework.http.HttpStatus;

import java.util.Set;

/**
 * Exception thrown when one or more supporting ApiCalls do not belong to the expected TestProject.
 */
public class SupportingApiCallInvalidException extends BusinessException {

    private static final String ERROR_CODE = "SUPPORTING_API_CALL_INVALID";

    public SupportingApiCallInvalidException(Long testProjectId, Set<Long> invalidIds) {
        super(
                String.format("One or more supporting ApiCall IDs are invalid or do not belong to TestProject %d: %s", testProjectId, invalidIds),
                ERROR_CODE,
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }
}
