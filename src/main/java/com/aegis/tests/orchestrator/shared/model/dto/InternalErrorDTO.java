package com.aegis.tests.orchestrator.shared.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.Objects;

/**
 * Error response for internal system errors (5xx errors).
 *
 * <p>Contains minimal information to avoid exposing internal system details.
 * The error code is included for support/debugging purposes, but the message
 * is always generic.</p>
 */
@Schema(description = "Internal error response - returned when an unexpected system error occurs")
public record InternalErrorDTO(

        @Schema(
                description = "Error code for support reference",
                example = "INVALID_STATUS_TRANSITION",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NonNull String errorCode,

        @Schema(
                description = "Generic error message (internal details are not exposed)",
                example = "An internal error occurred. Please try again later.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NonNull String message,

        @Schema(
                description = "Timestamp when the error occurred",
                example = "2026-01-27T23:30:00Z",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NonNull Instant timestamp

) {

    private static final String DEFAULT_MESSAGE = "An internal error occurred. Please try again later.";

    public InternalErrorDTO {
        Objects.requireNonNull(errorCode, "errorCode must not be null");
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    /**
     * Creates an InternalErrorDTO with the default generic message.
     *
     * @param errorCode Error code for debugging/support
     * @return InternalErrorDTO with generic message
     */
    public static InternalErrorDTO of(@NonNull String errorCode) {
        return new InternalErrorDTO(errorCode, DEFAULT_MESSAGE, Instant.now());
    }

    /**
     * Creates an InternalErrorDTO with a custom message.
     * Use sparingly - prefer the generic message to avoid exposing internal details.
     *
     * @param errorCode Error code for debugging/support
     * @param message   Custom message (should still be user-friendly, not technical)
     * @return InternalErrorDTO with custom message
     */
    public static InternalErrorDTO withMessage(@NonNull String errorCode, @NonNull String message) {
        return new InternalErrorDTO(errorCode, message != null ? message : DEFAULT_MESSAGE, Instant.now());
    }
}
