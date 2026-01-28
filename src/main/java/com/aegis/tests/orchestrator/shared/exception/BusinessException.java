package com.aegis.tests.orchestrator.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception for internal system errors that indicate bugs or unexpected states.
 *
 * <p>This exception hierarchy is for errors that <strong>ARE</strong> caused by user input
 * or business rule violations. These should result in HTTP 400 Client Side Error responses.</p>
 *
 * <p>Key differences from {@link InternalException}:</p>
 * <ul>
 *   <li>{@link BusinessException} = user/business error (4xx responses)</li>
 *   <li>{@link InternalException} = system/logic error (5xx responses)</li>
 * </ul>
 *
 * <p>Subclasses should provide specific error codes for logging and debugging purposes.
 * The actual error message returned to clients should be generic to avoid exposing
 * internal system details.</p>
 *
 * @see InternalException
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

