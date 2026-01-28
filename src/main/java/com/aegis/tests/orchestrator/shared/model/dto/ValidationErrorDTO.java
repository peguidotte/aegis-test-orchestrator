package com.aegis.tests.orchestrator.shared.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.Objects;

/**
 * Error response for field validation errors (Bean Validation failures).
 *
 * <p>Used when @Valid annotations fail. Contains field-specific error information.</p>
 */
@Schema(description = "Validation error response - returned when request payload validation fails")
public record ValidationErrorDTO(

        @Schema(
                description = "Standardized validation error code",
                example = "REQUIRED_FIELD",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NonNull String errorCode,

        @Schema(
                description = "Validation error message",
                example = "Name is required",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NonNull String message,

        @Schema(
                description = "Field name that failed validation",
                example = "name",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NonNull String field,

        @Schema(
                description = "API path that generated the error",
                example = "/v1/test-projects/123/specifications"
        )
        @Nullable String path,

        @Schema(
                description = "Timestamp when the error occurred",
                example = "2026-01-27T23:30:00Z",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NonNull Instant timestamp

) {

    public ValidationErrorDTO {
        Objects.requireNonNull(errorCode, "errorCode must not be null");
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(field, "field must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    public static ValidationErrorDTO of(@NonNull String errorCode, @NonNull String message, @NonNull String field) {
        return new ValidationErrorDTO(errorCode, message, field, null, Instant.now());
    }

    public static ValidationErrorDTO of(@NonNull String errorCode, @NonNull String message, @NonNull String field, @Nullable String path) {
        return new ValidationErrorDTO(errorCode, message, field, path, Instant.now());
    }
}
