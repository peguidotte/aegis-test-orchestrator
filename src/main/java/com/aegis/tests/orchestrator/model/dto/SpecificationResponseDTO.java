package com.aegis.tests.orchestrator.model.dto;

import com.aegis.tests.orchestrator.model.entity.ApiCall;
import com.aegis.tests.orchestrator.model.entity.Specification;
import com.aegis.tests.orchestrator.model.enums.HttpMethod;
import com.aegis.tests.orchestrator.model.enums.SpecStatus;
import com.aegis.tests.orchestrator.model.enums.SpecificationInputType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = "Response payload representing a Specification")
public record SpecificationResponseDTO(
        @Schema(description = "Specification unique identifier", example = "100")
        Long id,

        @Schema(description = "Input modality used to create this specification", example = "MANUAL")
        SpecificationInputType inputType,

        @Schema(description = "Specification name", example = "Create Invoice")
        String name,

        @Schema(description = "Functional description", example = "Creates a new invoice")
        String description,

        @Schema(description = "Detailed test objective", example = "Validate all invoice creation scenarios")
        String testObjective,

        @Schema(description = "HTTP method (resolved from ApiCall if API_CALL mode)", example = "POST")
        HttpMethod method,

        @Schema(description = "API path (resolved from ApiCall if API_CALL mode)", example = "/api/v1/invoices")
        String path,

        @Schema(description = "Whether authentication is required", example = "true")
        Boolean requiresAuth,

        @Schema(description = "Example request payload", example = "{\"customerId\": \"123\"}")
        String requestExample,

        @Schema(description = "Whether approval is required before test generation", example = "false")
        Boolean approveBeforeGeneration,

        @Schema(description = "Current status", example = "CREATED")
        SpecStatus status,

        @Schema(description = "TestProject ID", example = "1")
        Long testProjectId,

        @Schema(description = "Environment ID", example = "1")
        Long environmentId,

        @Schema(description = "Domain ID (optional)", example = "1")
        Long domainId,

        @Schema(description = "AuthProfile ID (optional)", example = "1")
        Long authProfileId,

        @Schema(description = "ApiCall ID (null if MANUAL mode)", example = "42")
        Long apiCallId,

        @Schema(description = "IDs of supporting ApiCalls for validation context", example = "[10, 11, 12]")
        Set<Long> supportingApiCallIds,

        @Schema(description = "Creation timestamp", example = "2026-01-17T14:00:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-17T14:00:00Z")
        Instant updatedAt,

        @Schema(description = "User who created", example = "john.doe")
        String createdBy
) {
    public static SpecificationResponseDTO fromEntity(Specification entity) {
        Set<Long> supportingIds = entity.getSupportingApiCalls() != null
                ? entity.getSupportingApiCalls().stream()
                        .map(ApiCall::getId)
                        .collect(Collectors.toSet())
                : Set.of();

        return new SpecificationResponseDTO(
                entity.getId(),
                entity.getInputType(),
                entity.getName(),
                entity.getDescription(),
                entity.getTestObjective(),
                entity.getMethod(),
                entity.getPath(),
                entity.getRequiresAuth(),
                entity.getRequestExample(),
                entity.getApproveBeforeGeneration(),
                entity.getStatus(),
                entity.getTestProject().getId(),
                entity.getEnvironment().getId(),
                entity.getDomain() != null ? entity.getDomain().getId() : null,
                entity.getAuthProfile() != null ? entity.getAuthProfile().getId() : null,
                entity.getApiCall() != null ? entity.getApiCall().getId() : null,
                supportingIds,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCreatedBy()
        );
    }
}

