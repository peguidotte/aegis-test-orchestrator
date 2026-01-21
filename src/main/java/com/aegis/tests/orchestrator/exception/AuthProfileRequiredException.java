package com.aegis.tests.orchestrator.exception;

import org.springframework.http.HttpStatus;

public class AuthProfileRequiredException extends BusinessException {
    private static final String ERROR_CODE = "AUTH_PROFILE_REQUIRED";

    public AuthProfileRequiredException() {
        super(
                "AuthProfile is required when requiresAuth is true",
                ERROR_CODE,
                HttpStatus.BAD_REQUEST,
                "authProfileId"
        );
    }
}

