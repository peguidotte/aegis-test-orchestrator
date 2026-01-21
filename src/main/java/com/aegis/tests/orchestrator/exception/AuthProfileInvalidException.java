package com.aegis.tests.orchestrator.exception;

import org.springframework.http.HttpStatus;

public class AuthProfileInvalidException extends BusinessException {
    private static final String ERROR_CODE = "AUTH_PROFILE_INVALID";

    public AuthProfileInvalidException(Long authProfileId, Long environmentId) {
        super(
                String.format("AuthProfile %d does not belong to Environment %d", authProfileId, environmentId),
                ERROR_CODE,
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }
}

