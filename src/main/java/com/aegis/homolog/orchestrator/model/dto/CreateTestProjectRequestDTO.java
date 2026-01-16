package com.aegis.homolog.orchestrator.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for creating a new TestProject")
public record CreateTestProjectRequestDTO(

        @Schema(description = "Test module identifier name", example = "Receivables Test Suite", maxLength = 255)
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must have at most 255 characters")
        String name,

        @Schema(description = "Brief description of the test module scope", example = "Integration tests for Invoices API", maxLength = 1000)
        @Size(max = 1000, message = "Description must have at most 1000 characters")
        String description
) {
}

