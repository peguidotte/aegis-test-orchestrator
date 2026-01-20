package com.aegis.homolog.orchestrator.model.dto;

import com.aegis.homolog.orchestrator.model.enums.HttpMethod;
import com.aegis.homolog.orchestrator.model.enums.SpecificationInputType;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(description = "Request payload for creating a new Specification")
public record CreateSpecificationRequestDTO(

        @Schema(
                description = "Input modality: MANUAL (provide method/path directly) or API_CALL (reference existing ApiCall)",
                example = "MANUAL",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "InputType is required")
        SpecificationInputType inputType,

        @Schema(description = "Specification name", example = "POST to Create Invoice tests", maxLength = 255)
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must have at most 255 characters")
        String name,

        @Schema(description = "Functional description", example = "Creates a new invoice", maxLength = 1000)
        @Size(max = 1000, message = "Description must have at most 1000 characters")
        String description,

        @Schema(
                description = "Detailed test objective describing what should be tested. Main context for AI.",
                example = "Validate all invoice creation scenarios including field validations, business rules, and error handling",
                maxLength = 2000,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "TestObjective is required")
        @Size(max = 2000, message = "TestObjective must have at most 2000 characters")
        String testObjective,

        @Schema(description = "Domain ID (optional)", example = "1")
        Long domainId,

        @Schema(
                description = "HTTP method. Required when inputType is MANUAL.",
                example = "POST"
        )
        HttpMethod method,

        @Schema(
                description = "API path starting with /. Required when inputType is MANUAL.",
                example = "/api/v1/invoices",
                maxLength = 500
        )
        @Size(max = 500, message = "Path must have at most 500 characters")
        @Pattern(regexp = "^/.*", message = "Path must start with /")
        String path,

        @Schema(
                description = "ApiCall ID from the endpoint catalog. Required when inputType is API_CALL.",
                example = "42"
        )
        Long apiCallId,

        @Schema(description = "Indicates if authentication is required", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "RequiresAuth is required")
        Boolean requiresAuth,

        @Schema(description = "AuthProfile ID (required if requiresAuth is true)", example = "1")
        Long authProfileId,

        @Schema(description = "Environment ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "EnvironmentId is required")
        Long environmentId,

        @Schema(description = "Example request payload as JSON object", example = "{\"customerId\": \"123\"}")
        JsonNode requestExample,

        @Schema(
                description = "IDs of additional ApiCalls that provide context for validation scenarios",
                example = "[10, 11, 12]"
        )
        Set<Long> supportingApiCallIds,

        @Schema(description = "If true, requires user approval before test generation", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ApproveBeforeGeneration is required")
        Boolean approveBeforeGeneration
) {
}

