package com.aegis.homolog.orchestrator.controller;

import com.aegis.homolog.orchestrator.exception.*;
import com.aegis.homolog.orchestrator.model.dto.CreateSpecificationRequestDTO;
import com.aegis.homolog.orchestrator.model.dto.SpecificationResponseDTO;
import com.aegis.homolog.orchestrator.model.enums.HttpMethod;
import com.aegis.homolog.orchestrator.model.enums.SpecStatus;
import com.aegis.homolog.orchestrator.model.enums.SpecificationInputType;
import com.aegis.homolog.orchestrator.services.SpecificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpecificationController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("SpecificationController")
class SpecificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SpecificationService specificationService;

    private static final Long TEST_PROJECT_ID = 1L;
    private static final Long ENVIRONMENT_ID = 10L;
    private static final Long DOMAIN_ID = 100L;
    private static final Long AUTH_PROFILE_ID = 200L;
    private static final Long API_CALL_ID = 300L;
    private static final String BASE_URL = "/v1/test-projects/{testProjectId}/specifications";

    @Nested
    @DisplayName("POST /v1/test-projects/{testProjectId}/specifications - MANUAL mode")
    class ManualModeTests {

        @Test
        @WithMockUser
        @DisplayName("should return 201 Created for valid MANUAL request")
        void shouldReturn201ForValidManualRequest() throws Exception {
            // Arrange
            var request = createValidManualRequest();
            var response = createManualSuccessResponse(request);

            when(specificationService.createSpecification(eq(TEST_PROJECT_ID), any(), any()))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(post(BASE_URL, TEST_PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(500))
                    .andExpect(jsonPath("$.inputType").value("MANUAL"))
                    .andExpect(jsonPath("$.name").value(request.name()))
                    .andExpect(jsonPath("$.testObjective").value(request.testObjective()))
                    .andExpect(jsonPath("$.method").value("POST"))
                    .andExpect(jsonPath("$.path").value(request.path()))
                    .andExpect(jsonPath("$.status").value("CREATED"))
                    .andExpect(jsonPath("$.apiCallId").isEmpty());
        }

        @Test
        @WithMockUser
        @DisplayName("should return 400 when name is blank")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            // Arrange
            var request = new CreateSpecificationRequestDTO(
                    SpecificationInputType.MANUAL,
                    "",  // blank name
                    "Description",
                    "Test objective",
                    null,
                    HttpMethod.POST,
                    "/api/v1/test",
                    null,
                    false,
                    null,
                    ENVIRONMENT_ID,
                    null,
                    null,
                    false
            );

            // Act & Assert
            mockMvc.perform(post(BASE_URL, TEST_PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$[0].errorCode").value("REQUIRED_FIELD"))
                    .andExpect(jsonPath("$[0].field").value("name"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 400 when testObjective is blank")
        void shouldReturn400WhenTestObjectiveIsBlank() throws Exception {
            // Arrange
            var request = new CreateSpecificationRequestDTO(
                    SpecificationInputType.MANUAL,
                    "Test Name",
                    "Description",
                    "",  // blank testObjective
                    null,
                    HttpMethod.POST,
                    "/api/v1/test",
                    null,
                    false,
                    null,
                    ENVIRONMENT_ID,
                    null,
                    null,
                    false
            );

            // Act & Assert
            mockMvc.perform(post(BASE_URL, TEST_PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$[0].errorCode").value("REQUIRED_FIELD"))
                    .andExpect(jsonPath("$[0].field").value("testObjective"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 400 when path does not start with /")
        void shouldReturn400WhenPathInvalid() throws Exception {
            // Arrange
            var request = new CreateSpecificationRequestDTO(
                    SpecificationInputType.MANUAL,
                    "Test Name",
                    "Description",
                    "Test objective",
                    null,
                    HttpMethod.POST,
                    "api/v1/invoices",  // missing leading /
                    null,
                    false,
                    null,
                    ENVIRONMENT_ID,
                    null,
                    null,
                    false
            );

            // Act & Assert
            mockMvc.perform(post(BASE_URL, TEST_PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$[0].errorCode").value("INVALID_FORMAT"))
                    .andExpect(jsonPath("$[0].field").value("path"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 400 when inputType is null")
        void shouldReturn400WhenInputTypeIsNull() throws Exception {
            // Arrange - using JSON directly to send null inputType
            String requestJson = """
                {
                    "inputType": null,
                    "name": "Create Invoice",
                    "testObjective": "Test objective",
                    "method": "POST",
                    "path": "/api/v1/invoices",
                    "requiresAuth": false,
                    "environmentId": 10,
                    "approveBeforeGeneration": false
                }
                """;

            // Act & Assert
            mockMvc.perform(post(BASE_URL, TEST_PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$[0].errorCode").value("REQUIRED_FIELD"))
                    .andExpect(jsonPath("$[0].field").value("inputType"));
        }
    }

    @Nested
    @DisplayName("POST /v1/test-projects/{testProjectId}/specifications - API_CALL mode")
    class ApiCallModeTests {

        @Test
        @WithMockUser
        @DisplayName("should return 201 Created for valid API_CALL request")
        void shouldReturn201ForValidApiCallRequest() throws Exception {
            // Arrange
            var request = createValidApiCallRequest();
            var response = createApiCallSuccessResponse(request);

            when(specificationService.createSpecification(eq(TEST_PROJECT_ID), any(), any()))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(post(BASE_URL, TEST_PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(500))
                    .andExpect(jsonPath("$.inputType").value("API_CALL"))
                    .andExpect(jsonPath("$.apiCallId").value(API_CALL_ID))
                    .andExpect(jsonPath("$.method").value("POST"))
                    .andExpect(jsonPath("$.path").value("/api/v1/invoices"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 404 when ApiCall not found")
        void shouldReturn404WhenApiCallNotFound() throws Exception {
            // Arrange
            var request = createValidApiCallRequest();

            when(specificationService.createSpecification(eq(TEST_PROJECT_ID), any(), any()))
                    .thenThrow(new ApiCallNotFoundException(API_CALL_ID));

            // Act & Assert
            mockMvc.perform(post(BASE_URL, TEST_PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$[0].errorCode").value("API_CALL_NOT_FOUND"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 422 when ApiCall belongs to different project")
        void shouldReturn422WhenApiCallInvalid() throws Exception {
            // Arrange
            var request = createValidApiCallRequest();

            when(specificationService.createSpecification(eq(TEST_PROJECT_ID), any(), any()))
                    .thenThrow(new ApiCallInvalidException(API_CALL_ID, TEST_PROJECT_ID));

            // Act & Assert
            mockMvc.perform(post(BASE_URL, TEST_PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$[0].errorCode").value("API_CALL_INVALID"));
        }
    }

    @Nested
    @DisplayName("POST /v1/test-projects/{testProjectId}/specifications - Common validations")
    class CommonValidationTests {

        @Test
        @WithMockUser
        @DisplayName("should return 404 when TestProject not found")
        void shouldReturn404WhenTestProjectNotFound() throws Exception {
            // Arrange
            var request = createValidManualRequest();

            when(specificationService.createSpecification(eq(TEST_PROJECT_ID), any(), any()))
                    .thenThrow(new TestProjectNotFoundException(TEST_PROJECT_ID));

            // Act & Assert
            mockMvc.perform(post(BASE_URL, TEST_PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$[0].errorCode").value("TEST_PROJECT_NOT_FOUND"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 404 when Environment not found")
        void shouldReturn404WhenEnvironmentNotFound() throws Exception {
            // Arrange
            var request = createValidManualRequest();

            when(specificationService.createSpecification(eq(TEST_PROJECT_ID), any(), any()))
                    .thenThrow(new EnvironmentNotFoundException(ENVIRONMENT_ID, TEST_PROJECT_ID));

            // Act & Assert
            mockMvc.perform(post(BASE_URL, TEST_PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$[0].errorCode").value("ENVIRONMENT_NOT_FOUND"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 400 when authProfileId required but missing")
        void shouldReturn400WhenAuthProfileRequired() throws Exception {
            // Arrange
            var request = new CreateSpecificationRequestDTO(
                    SpecificationInputType.MANUAL,
                    "Test Name",
                    "Description",
                    "Test objective",
                    null,
                    HttpMethod.POST,
                    "/api/v1/invoices",
                    null,
                    true,   // requiresAuth = true
                    null,   // authProfileId = null
                    ENVIRONMENT_ID,
                    null,
                    null,
                    false
            );

            when(specificationService.createSpecification(eq(TEST_PROJECT_ID), any(), any()))
                    .thenThrow(new AuthProfileRequiredException());

            // Act & Assert
            mockMvc.perform(post(BASE_URL, TEST_PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$[0].errorCode").value("AUTH_PROFILE_REQUIRED"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 422 when AuthProfile invalid")
        void shouldReturn422WhenAuthProfileInvalid() throws Exception {
            // Arrange
            var request = createValidManualRequest();

            when(specificationService.createSpecification(eq(TEST_PROJECT_ID), any(), any()))
                    .thenThrow(new AuthProfileInvalidException(AUTH_PROFILE_ID, ENVIRONMENT_ID));

            // Act & Assert
            mockMvc.perform(post(BASE_URL, TEST_PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$[0].errorCode").value("AUTH_PROFILE_INVALID"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 422 when Domain not found")
        void shouldReturn422WhenDomainNotFound() throws Exception {
            // Arrange
            var request = createValidManualRequest();

            when(specificationService.createSpecification(eq(TEST_PROJECT_ID), any(), any()))
                    .thenThrow(new DomainNotFoundException(DOMAIN_ID));

            // Act & Assert
            mockMvc.perform(post(BASE_URL, TEST_PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$[0].errorCode").value("DOMAIN_NOT_FOUND"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 409 when Specification already exists")
        void shouldReturn409WhenSpecificationAlreadyExists() throws Exception {
            // Arrange
            var request = createValidManualRequest();

            when(specificationService.createSpecification(eq(TEST_PROJECT_ID), any(), any()))
                    .thenThrow(new SpecificationAlreadyExistsException(
                            HttpMethod.POST, "/api/v1/invoices", ENVIRONMENT_ID));

            // Act & Assert
            mockMvc.perform(post(BASE_URL, TEST_PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$[0].errorCode").value("SPECIFICATION_ALREADY_EXISTS"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 422 when supporting ApiCalls invalid")
        void shouldReturn422WhenSupportingApiCallsInvalid() throws Exception {
            // Arrange
            var request = new CreateSpecificationRequestDTO(
                    SpecificationInputType.MANUAL,
                    "Test Name",
                    "Description",
                    "Test objective",
                    null,
                    HttpMethod.POST,
                    "/api/v1/invoices",
                    null,
                    false,
                    null,
                    ENVIRONMENT_ID,
                    null,
                    Set.of(999L, 888L),  // invalid IDs
                    false
            );

            when(specificationService.createSpecification(eq(TEST_PROJECT_ID), any(), any()))
                    .thenThrow(new SupportingApiCallInvalidException(TEST_PROJECT_ID, Set.of(999L)));

            // Act & Assert
            mockMvc.perform(post(BASE_URL, TEST_PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$[0].errorCode").value("SUPPORTING_API_CALL_INVALID"));
        }
    }

    // Helper methods

    private CreateSpecificationRequestDTO createValidManualRequest() {
        return new CreateSpecificationRequestDTO(
                SpecificationInputType.MANUAL,
                "Create Invoice",
                "Creates a new invoice in the system",
                "Validate all invoice creation scenarios including field validations and business rules",
                DOMAIN_ID,
                HttpMethod.POST,
                "/api/v1/invoices",
                null,  // no apiCallId for MANUAL
                true,
                AUTH_PROFILE_ID,
                ENVIRONMENT_ID,
                createJsonNode("{\"customerId\": \"123\", \"amount\": 100.50}"),
                null,
                false
        );
    }

    private CreateSpecificationRequestDTO createValidApiCallRequest() {
        return new CreateSpecificationRequestDTO(
                SpecificationInputType.API_CALL,
                "Invoice Tests",
                "Comprehensive invoice tests",
                "Validate all invoice scenarios",
                null,
                null,  // method resolved from ApiCall
                null,  // path resolved from ApiCall
                API_CALL_ID,
                true,
                AUTH_PROFILE_ID,
                ENVIRONMENT_ID,
                null,
                null,
                false
        );
    }

    private SpecificationResponseDTO createManualSuccessResponse(CreateSpecificationRequestDTO request) {
        return new SpecificationResponseDTO(
                500L,
                SpecificationInputType.MANUAL,
                request.name(),
                request.description(),
                request.testObjective(),
                request.method(),
                request.path(),
                request.requiresAuth(),
                jsonNodeToString(request.requestExample()),
                request.approveBeforeGeneration(),
                SpecStatus.CREATED,
                TEST_PROJECT_ID,
                ENVIRONMENT_ID,
                request.domainId(),
                request.authProfileId(),
                null,  // apiCallId
                Set.of(),  // supportingApiCallIds
                Instant.parse("2026-01-17T14:00:00Z"),
                Instant.parse("2026-01-17T14:00:00Z"),
                "system-user"
        );
    }

    private SpecificationResponseDTO createApiCallSuccessResponse(CreateSpecificationRequestDTO request) {
        return new SpecificationResponseDTO(
                500L,
                SpecificationInputType.API_CALL,
                request.name(),
                request.description(),
                request.testObjective(),
                HttpMethod.POST,  // resolved from ApiCall
                "/api/v1/invoices",  // resolved from ApiCall
                request.requiresAuth(),
                jsonNodeToString(request.requestExample()),
                request.approveBeforeGeneration(),
                SpecStatus.CREATED,
                TEST_PROJECT_ID,
                ENVIRONMENT_ID,
                DOMAIN_ID,  // from ApiCall
                request.authProfileId(),
                API_CALL_ID,
                Set.of(),
                Instant.parse("2026-01-17T14:00:00Z"),
                Instant.parse("2026-01-17T14:00:00Z"),
                "system-user"
        );
    }

    private JsonNode createJsonNode(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON: " + json, e);
        }
    }

    private String jsonNodeToString(JsonNode node) {
        return node != null ? node.toString() : null;
    }
}

