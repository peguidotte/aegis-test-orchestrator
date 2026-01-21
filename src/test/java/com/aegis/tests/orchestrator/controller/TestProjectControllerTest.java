package com.aegis.tests.orchestrator.controller;

import com.aegis.tests.orchestrator.exception.GlobalExceptionHandler;
import com.aegis.tests.orchestrator.exception.TestProjectLimitReachedException;
import com.aegis.tests.orchestrator.exception.TestProjectNameAlreadyExistsException;
import com.aegis.tests.orchestrator.model.dto.CreateTestProjectRequestDTO;
import com.aegis.tests.orchestrator.model.dto.TestProjectResponseDTO;
import com.aegis.tests.orchestrator.services.TestProjectService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TestProjectController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("TestProjectController")
class TestProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TestProjectService testProjectService;

    private static final Long PROJECT_ID = 10L;
    private static final String BASE_URL = "/v1/projects/{projectId}/test-projects";

    @Nested
    @DisplayName("POST /v1/projects/{projectId}/test-projects")
    class CreateTestProjectTests {

        @Test
        @WithMockUser
        @DisplayName("should return 201 Created when TestProject is created successfully")
        void shouldReturn201WhenCreatedSuccessfully() throws Exception {
            // Arrange
            var request = new CreateTestProjectRequestDTO(
                    "Receivables Test Suite",
                    "Integration tests for Invoices API"
            );

            var response = new TestProjectResponseDTO(
                    500L,
                    request.name(),
                    PROJECT_ID,
                    request.description(),
                    Instant.parse("2026-01-12T14:00:00Z"),
                    "system-user"
            );

            when(testProjectService.create(eq(PROJECT_ID), any(CreateTestProjectRequestDTO.class), any()))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(post(BASE_URL, PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(500))
                    .andExpect(jsonPath("$.name").value(request.name()))
                    .andExpect(jsonPath("$.projectId").value(PROJECT_ID))
                    .andExpect(jsonPath("$.description").value(request.description()))
                    .andExpect(jsonPath("$.createdBy").value("system-user"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 400 Bad Request when name is blank (RC10.01.1)")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            // Arrange
            var request = new CreateTestProjectRequestDTO("", "Description");

            // Act & Assert
            mockMvc.perform(post(BASE_URL, PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$[0].errorCode").value("REQUIRED_FIELD"))
                    .andExpect(jsonPath("$[0].field").value("name"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 400 Bad Request when name exceeds 255 characters (RC10.01.1)")
        void shouldReturn400WhenNameExceedsMaxLength() throws Exception {
            // Arrange
            var longName = "A".repeat(256);
            var request = new CreateTestProjectRequestDTO(longName, "Description");

            // Act & Assert
            mockMvc.perform(post(BASE_URL, PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$[0].errorCode").value("INVALID_FIELD_LENGTH"))
                    .andExpect(jsonPath("$[0].field").value("name"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 400 Bad Request when description exceeds 1000 characters (RC10.01.2)")
        void shouldReturn400WhenDescriptionExceedsMaxLength() throws Exception {
            // Arrange
            var longDescription = "A".repeat(1001);
            var request = new CreateTestProjectRequestDTO("Valid Name", longDescription);

            // Act & Assert
            mockMvc.perform(post(BASE_URL, PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$[0].errorCode").value("INVALID_FIELD_LENGTH"))
                    .andExpect(jsonPath("$[0].field").value("description"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 422 Unprocessable Entity when limit is reached (RN10.01.2)")
        void shouldReturn422WhenLimitReached() throws Exception {
            // Arrange
            var request = new CreateTestProjectRequestDTO("New Test Project", "Description");

            when(testProjectService.create(eq(PROJECT_ID), any(), any()))
                    .thenThrow(new TestProjectLimitReachedException(PROJECT_ID));

            // Act & Assert
            mockMvc.perform(post(BASE_URL, PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$[0].errorCode").value("TEST_PROJECT_LIMIT_REACHED"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 409 Conflict when name already exists (RN10.01.3)")
        void shouldReturn409WhenNameAlreadyExists() throws Exception {
            // Arrange
            var request = new CreateTestProjectRequestDTO("Existing Name", "Description");

            when(testProjectService.create(eq(PROJECT_ID), any(), any()))
                    .thenThrow(new TestProjectNameAlreadyExistsException(request.name(), PROJECT_ID));

            // Act & Assert
            mockMvc.perform(post(BASE_URL, PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$[0].errorCode").value("TEST_PROJECT_NAME_ALREADY_EXISTS"))
                    .andExpect(jsonPath("$[0].field").value("name"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 201 Created with null description")
        void shouldReturn201WithNullDescription() throws Exception {
            // Arrange
            var request = new CreateTestProjectRequestDTO("Simple Test Suite", null);

            var response = new TestProjectResponseDTO(
                    501L,
                    request.name(),
                    PROJECT_ID,
                    null,
                    Instant.now(),
                    "system-user"
            );

            when(testProjectService.create(eq(PROJECT_ID), any(), any())).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post(BASE_URL, PROJECT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.description").doesNotExist());
        }
    }
}

