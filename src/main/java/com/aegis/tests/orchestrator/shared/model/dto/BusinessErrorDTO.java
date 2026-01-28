package com.aegis.tests.orchestrator.shared.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.Objects;

/**
 * Error response for business rule violations (4xx errors).
 *
 * <p>Contains detailed information to help the client understand and correct the error.</p>
 */
@Schema(description = "Business error response - returned when a business rule is violated")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BusinessErrorDTO(

        @Schema(
                description = "Standardized error code for programmatic handling",
                example = "RESOURCE_NOT_FOUND",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NonNull String errorCode,

        @Schema(
                description = "User-friendly error message explaining what went wrong",
                example = "The requested test project was not found",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NonNull String message,

        @Schema(
                description = "Field name that caused the error (when applicable)",
                example = "testProjectId"
        )
        @Nullable String field,

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

    public BusinessErrorDTO {
        Objects.requireNonNull(errorCode, "errorCode must not be null");
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    public static BusinessErrorDTO of(@NonNull String errorCode, @NonNull String message) {
        return new BusinessErrorDTO(errorCode, message, null, null, Instant.now());
    }

    public static BusinessErrorDTO of(@NonNull String errorCode, @NonNull String message, @Nullable String field) {
        return new BusinessErrorDTO(errorCode, message, field, null, Instant.now());
    }

    public static BusinessErrorDTO of(@NonNull String errorCode, @NonNull String message, @Nullable String field, @Nullable String path) {
        return new BusinessErrorDTO(errorCode, message, field, path, Instant.now());
    }
}
