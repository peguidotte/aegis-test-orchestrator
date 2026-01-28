package com.aegis.tests.orchestrator.specification;

import com.aegis.tests.orchestrator.shared.model.dto.BusinessErrorDTO;
import com.aegis.tests.orchestrator.shared.model.dto.ValidationErrorDTO;
import com.aegis.tests.orchestrator.specification.dto.CreateSpecificationRequestDTO;
import com.aegis.tests.orchestrator.specification.dto.SpecificationResponseDTO;
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
@RequestMapping("/v1/test-projects/{testProjectId}/specifications")
@Tag(name = "Specifications", description = "API specification management endpoints for AI-driven test generation")
public class SpecificationController {

    private final SpecificationService specificationService;

    public SpecificationController(SpecificationService specificationService) {
        this.specificationService = specificationService;
    }

    @Operation(
            summary = "Create Specification",
            description = """
                    Creates a new API test specification that serves as the functional source of truth
                    for AI-driven test generation.

                    **Input Modalities:**
                    - `MANUAL`: Provide method, path, and request example directly in the request.
                    - `API_CALL`: Reference an existing ApiCall from the endpoint catalog by its ID.
                      Method and path will be resolved automatically from the ApiCall.

                    **Status Logic:**
                    - If `approveBeforeGeneration = false`: Status will be `CREATED` and the AI agent
                      will be notified immediately to start test generation.
                    - If `approveBeforeGeneration = true`: Status will be `WAITING_APPROVAL` and
                      manual approval will be required before test generation begins.

                    **Conditional Fields:**
                    - `method` and `path`: Required when `inputType = MANUAL`
                    - `apiCallId`: Required when `inputType = API_CALL`
                    - `authProfileId`: Required when `requiresAuth = true`
                    """
    )
    @ApiResponse(
            responseCode = "201",
            description = "Specification created successfully",
            content = @Content(schema = @Schema(implementation = SpecificationResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationErrorDTO.class))
    )
    @ApiResponse(
            responseCode = "4xx",
            description = "Business rule violated",
            content = @Content(schema = @Schema(implementation = BusinessErrorDTO.class))
    )
    @PostMapping
    public ResponseEntity<SpecificationResponseDTO> createSpecification(
            @PathVariable Long testProjectId,
            @Valid @RequestBody CreateSpecificationRequestDTO request
    ) {
        // TODO: Extract userId from JWT token (RN10.01.1)
        String userId = "system-user";

        SpecificationResponseDTO response = specificationService.createSpecification(
                testProjectId, request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

