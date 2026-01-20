package com.aegis.homolog.orchestrator.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Objects;

@Schema(description = "Standard error response object")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDTO(

        @Schema(description = "Internal standardized error code", example = "ERROR_CODE_HERE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NonNull String errorCode,

        @Schema(description = "User-friendly error message", example = "User-friendly error message here", requiredMode = Schema.RequiredMode.REQUIRED)
        @NonNull String message,

        @Schema(description = "Field name that caused the error (when applicable)", example = "Field name if applicable")
        @Nullable String field
) {

    /**
     * Compact constructor to validate non-null fields.
     */
    public ErrorResponseDTO {
        Objects.requireNonNull(errorCode, "errorCode must not be null");
        Objects.requireNonNull(message, "message must not be null");
    }

    public static ErrorResponseDTO of(@NonNull String errorCode, @NonNull String message) {
        return new ErrorResponseDTO(errorCode, message, null);
    }

    public static ErrorResponseDTO of(@NonNull String errorCode, @NonNull String message, @Nullable String field) {
        return new ErrorResponseDTO(errorCode, message, field);
    }
}

