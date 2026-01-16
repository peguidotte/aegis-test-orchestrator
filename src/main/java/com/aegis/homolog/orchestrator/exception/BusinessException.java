package com.aegis.homolog.orchestrator.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception to all business rule violations.
 * Subclasses define specific error codes and HTTP status.
 */
@Getter
public abstract class BusinessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;
    private final String field;

    protected BusinessException(String message, String errorCode, HttpStatus httpStatus) {
        this(message, errorCode, httpStatus, null);
    }

    protected BusinessException(String message, String errorCode, HttpStatus httpStatus, String field) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.field = field;
    }

}

