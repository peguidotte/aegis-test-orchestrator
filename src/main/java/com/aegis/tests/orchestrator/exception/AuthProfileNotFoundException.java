package com.aegis.tests.orchestrator.exception;

import org.springframework.http.HttpStatus;

public class AuthProfileNotFoundException extends BusinessException {
    private static final String ERROR_CODE = "AUTH_PROFILE_NOT_FOUND";

    public AuthProfileNotFoundException(Long authProfileId) {
        super(
                String.format("AuthProfile with ID %d not found", authProfileId),
                ERROR_CODE,
                HttpStatus.NOT_FOUND
        );
    }
}

