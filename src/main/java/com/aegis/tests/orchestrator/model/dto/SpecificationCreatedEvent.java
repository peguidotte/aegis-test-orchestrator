package com.aegis.tests.orchestrator.model.dto;

import com.aegis.tests.orchestrator.model.entity.ApiCall;
import com.aegis.tests.orchestrator.model.entity.Specification;
import com.aegis.tests.orchestrator.model.enums.SpecificationInputType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = "Event published when a Specification is created")
public record SpecificationCreatedEvent(
        @Schema(description = "Specification ID", example = "100")
        Long specificationId,
        @Schema(description = "TestProject ID", example = "1")
        Long testProjectId,
        @Schema(description = "Environment ID", example = "1")
        Long environmentId,
        @Schema(description = "Input modality used", example = "MANUAL")
        SpecificationInputType inputType,
        @Schema(description = "HTTP method", example = "POST")
        String method,
        @Schema(description = "API path", example = "/api/v1/invoices")
        String path,
        @Schema(description = "Detailed test objective for AI context")
        String testObjective,
        @Schema(description = "Example request payload")
        String requestExample,
        @Schema(description = "IDs of supporting ApiCalls for context")
        Set<Long> supportingApiCallIds,
        @Schema(description = "When the specification was created")
        Instant createdAt
) {
    public static SpecificationCreatedEvent fromEntity(Specification entity) {
        Set<Long> supportingIds = entity.getSupportingApiCalls() != null
                ? entity.getSupportingApiCalls().stream()
                        .map(ApiCall::getId)
                        .collect(Collectors.toSet())
                : Set.of();

        return new SpecificationCreatedEvent(
                entity.getId(),
                entity.getTestProject().getId(),
                entity.getEnvironment().getId(),
                entity.getInputType(),
                entity.getMethod().name(),
                entity.getPath(),
                entity.getTestObjective(),
                entity.getRequestExample(),
                supportingIds,
                entity.getCreatedAt()
        );
    }
}

