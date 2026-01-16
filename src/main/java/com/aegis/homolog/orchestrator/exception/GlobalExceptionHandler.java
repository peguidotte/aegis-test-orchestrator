package com.aegis.homolog.orchestrator.exception;

import com.aegis.homolog.orchestrator.model.dto.ErrorResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Global exception handler that converts exceptions to standardized API responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles all BusinessException subclasses.
     * Returns the appropriate HTTP status and error response.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<List<ErrorResponseDTO>> handleBusinessException(BusinessException ex) {
        var error = ErrorResponseDTO.of(ex.getErrorCode(), ex.getMessage(), ex.getField());
        return ResponseEntity.status(ex.getHttpStatus()).body(List.of(error));
    }

    /**
     * Handles validation errors from @Valid annotations.
     * Returns 400 Bad Request with all field errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErrorResponseDTO>> handleValidationException(MethodArgumentNotValidException ex) {
        List<ErrorResponseDTO> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> ErrorResponseDTO.of(
                        mapConstraintToErrorCode(fieldError.getCode()),
                        fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value",
                        fieldError.getField()
                ))
                .toList();

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Maps Bean Validation constraint names to standardized error codes.
     */
    private String mapConstraintToErrorCode(String constraintName) {
        if (constraintName == null) {
            return "VALIDATION_ERROR";
        }

        return switch (constraintName) {
            case "NotBlank", "NotNull", "NotEmpty" -> "REQUIRED_FIELD";
            case "Size", "Length" -> "INVALID_FIELD_LENGTH";
            case "Email" -> "INVALID_EMAIL_FORMAT";
            case "Pattern" -> "INVALID_FORMAT";
            case "Min", "Max", "DecimalMin", "DecimalMax" -> "INVALID_VALUE_RANGE";
            case "Positive", "PositiveOrZero", "Negative", "NegativeOrZero" -> "INVALID_NUMBER";
            case "Past", "PastOrPresent", "Future", "FutureOrPresent" -> "INVALID_DATE";
            default -> "VALIDATION_ERROR";
        };
    }
}

