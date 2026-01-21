package com.aegis.tests.orchestrator.controller;

import com.aegis.tests.orchestrator.model.dto.CreateTestProjectRequestDTO;
import com.aegis.tests.orchestrator.model.dto.ErrorResponseDTO;
import com.aegis.tests.orchestrator.model.dto.TestProjectResponseDTO;
import com.aegis.tests.orchestrator.services.TestProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/projects/{projectId}/test-projects")
@Tag(name = "Test Projects", description = "Test project management endpoints")
public class TestProjectController {

    private final TestProjectService testProjectService;

    public TestProjectController(TestProjectService testProjectService) {
        this.testProjectService = testProjectService;
    }

    @Operation(
            summary = "Create Test Project",
            description = "Creates a TestProject as the root container for all test-related entities. Inherits member permissions from the Core Project."
    )
    @ApiResponse(responseCode = "201", description = "TestProject created successfully",
            content = @Content(schema = @Schema(implementation = TestProjectResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "TestProject name already exists in project",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "422", description = "Business rule violated - TestProject limit reached",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PostMapping
    public ResponseEntity<TestProjectResponseDTO> create(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTestProjectRequestDTO request
    ) {
        // TODO: Extract userId from JWT token (RN10.01.1)
        String userId = "system-user";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(testProjectService.create(projectId, request, userId));
    }
}

