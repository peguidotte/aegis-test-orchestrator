package com.aegis.tests.orchestrator.shared.exception;

import com.aegis.tests.orchestrator.shared.model.dto.BusinessErrorDTO;
import com.aegis.tests.orchestrator.shared.model.dto.InternalErrorDTO;
import com.aegis.tests.orchestrator.shared.model.dto.ValidationErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Global exception handler that converts exceptions to standardized API responses.
 *
 * <p>Handles three main categories of exceptions, each with its own response DTO:</p>
 * <ul>
 *   <li>{@link BusinessException} → {@link BusinessErrorDTO} (4xx responses)</li>
 *   <li>{@link InternalException} → {@link InternalErrorDTO} (5xx responses)</li>
 *   <li>{@link MethodArgumentNotValidException} → {@link ValidationErrorDTO} (400 response)</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles all BusinessException subclasses.
     * Returns the appropriate HTTP status and detailed error information.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<List<BusinessErrorDTO>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        var error = BusinessErrorDTO.of(ex.getErrorCode(), ex.getMessage(), ex.getField(), request.getRequestURI());
        return ResponseEntity.status(ex.getHttpStatus()).body(List.of(error));
    }

    /**
     * Handles validation errors from @Valid annotations.
     * Returns 400 Bad Request with all field errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ValidationErrorDTO>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ValidationErrorDTO> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> ValidationErrorDTO.of(
                        mapConstraintToErrorCode(fieldError.getCode()),
                        fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value",
                        fieldError.getField(),
                        request.getRequestURI()
                ))
                .toList();

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handles all InternalException subclasses (system/logic errors).
     * Returns 500 Internal Server Error with minimal information.
     * The error is logged for debugging but only the error code is exposed to the client.
     */
    @ExceptionHandler(InternalException.class)
    public ResponseEntity<InternalErrorDTO> handleInternalException(InternalException ex) {
        log.error("Internal error [{}]: {}", ex.getErrorCode(), ex.getMessage(), ex);
        var error = InternalErrorDTO.of(ex.getErrorCode());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
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

