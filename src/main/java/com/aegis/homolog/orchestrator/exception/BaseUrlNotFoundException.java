package com.aegis.homolog.orchestrator.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a BaseUrl is not found.
 */
public class BaseUrlNotFoundException extends BusinessException {

    private static final String ERROR_CODE = "BASE_URL_NOT_FOUND";

    public BaseUrlNotFoundException(Long baseUrlId) {
        super(
                String.format("BaseUrl with ID %d not found", baseUrlId),
                ERROR_CODE,
                HttpStatus.NOT_FOUND
        );
    }
}
