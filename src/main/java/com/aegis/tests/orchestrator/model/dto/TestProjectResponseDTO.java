package com.aegis.tests.orchestrator.model.dto;

import com.aegis.tests.orchestrator.model.entity.TestProject;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Response payload representing a TestProject")
public record TestProjectResponseDTO(

        @Schema(description = "TestProject unique identifier", example = "500")
        Long id,

        @Schema(description = "Test module identifier name", example = "Receivables Test Suite")
        String name,

        @Schema(description = "Associated Core Project ID", example = "10")
        Long projectId,

        @Schema(description = "Test module scope description", example = "Integration tests for Invoices API")
        String description,

        @Schema(description = "TestProject creation timestamp", example = "2026-01-12T14:00:00Z")
        Instant createdAt,

        @Schema(description = "User who created the TestProject", example = "john.doe")
        String createdBy
) {

    public static TestProjectResponseDTO fromEntity(TestProject entity) {
        return new TestProjectResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getProjectId(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getCreatedBy()
        );
    }
}

