package com.aegis.tests.orchestrator.shared.exception;

import lombok.Getter;

/**
 * Base exception for internal system errors that indicate bugs or unexpected states.
 *
 * <p>This exception hierarchy is for errors that are <strong>NOT</strong> caused by user input
 * or business rule violations, but rather by system logic failures. These should result
 * in HTTP 500 Internal Server Error responses.</p>
 *
 * <p>Key differences from {@link BusinessException}:</p>
 * <ul>
 *   <li>{@link BusinessException} = user/business error (4xx responses)</li>
 *   <li>{@link InternalException} = system/logic error (5xx responses)</li>
 * </ul>
 *
 * <p>Subclasses should provide specific error codes for logging and debugging purposes.
 * The actual error message returned to clients should be generic to avoid exposing
 * internal system details.</p>
 *
 * @see BusinessException
 */
@Getter
public abstract class InternalException extends RuntimeException {

    /**
     * Error code for logging and debugging (not exposed to clients).
     */
    private final String errorCode;

    protected InternalException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    protected InternalException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
