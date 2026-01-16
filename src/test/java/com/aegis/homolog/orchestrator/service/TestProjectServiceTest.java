package com.aegis.homolog.orchestrator.service;

import com.aegis.homolog.orchestrator.exception.TestProjectLimitReachedException;
import com.aegis.homolog.orchestrator.exception.TestProjectNameAlreadyExistsException;
import com.aegis.homolog.orchestrator.model.dto.CreateTestProjectRequestDTO;
import com.aegis.homolog.orchestrator.model.dto.TestProjectResponseDTO;
import com.aegis.homolog.orchestrator.model.entity.Environment;
import com.aegis.homolog.orchestrator.model.entity.TestProject;
import com.aegis.homolog.orchestrator.repository.TestProjectRepository;
import com.aegis.homolog.orchestrator.services.EnvironmentService;
import com.aegis.homolog.orchestrator.services.TestProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestProjectService")
class TestProjectServiceTest {

    @Mock
    private TestProjectRepository testProjectRepository;

    @Mock
    private EnvironmentService environmentService;

    @InjectMocks
    private TestProjectService testProjectService;

    @Captor
    private ArgumentCaptor<TestProject> testProjectCaptor;


    private static final Long PROJECT_ID = 10L;
    private static final String USER_ID = "user-123";

    @Nested
    @DisplayName("create")
    class CreateTestProjectTests {

        private CreateTestProjectRequestDTO validRequest;

        @BeforeEach
        void setUp() {
            validRequest = new CreateTestProjectRequestDTO(
                    "Receivables Test Suite",
                    "Integration tests for Invoices API"
            );
        }

        @Test
        @DisplayName("should create TestProject successfully when no existing projects")
        void shouldCreateTestProjectSuccessfully() {
            // Arrange
            when(testProjectRepository.countByProjectId(PROJECT_ID)).thenReturn(0L);
            when(testProjectRepository.findByProjectIdAndName(PROJECT_ID, validRequest.name()))
                    .thenReturn(Optional.empty());

            TestProject savedProject = createTestProject(500L, validRequest.name(), validRequest.description());
            when(testProjectRepository.save(any(TestProject.class))).thenReturn(savedProject);

            Environment savedEnvironment = createDefaultEnvironment(savedProject);
            when(environmentService.createDefault(any(TestProject.class), eq(USER_ID))).thenReturn(savedEnvironment);

            // Act
            TestProjectResponseDTO response = testProjectService.create(PROJECT_ID, validRequest, USER_ID);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(500L);
            assertThat(response.name()).isEqualTo(validRequest.name());
            assertThat(response.projectId()).isEqualTo(PROJECT_ID);
            assertThat(response.description()).isEqualTo(validRequest.description());
            assertThat(response.createdBy()).isEqualTo(USER_ID);

            verify(testProjectRepository).save(testProjectCaptor.capture());
            TestProject captured = testProjectCaptor.getValue();
            assertThat(captured.getProjectId()).isEqualTo(PROJECT_ID);
            assertThat(captured.getName()).isEqualTo(validRequest.name());
            assertThat(captured.getCreatedBy()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("should create default Environment automatically when TestProject is created")
        void shouldCreateDefaultEnvironmentAutomatically() {
            // Arrange
            when(testProjectRepository.countByProjectId(PROJECT_ID)).thenReturn(0L);
            when(testProjectRepository.findByProjectIdAndName(PROJECT_ID, validRequest.name()))
                    .thenReturn(Optional.empty());

            TestProject savedProject = createTestProject(500L, validRequest.name(), validRequest.description());
            when(testProjectRepository.save(any(TestProject.class))).thenReturn(savedProject);

            Environment savedEnvironment = createDefaultEnvironment(savedProject);
            when(environmentService.createDefault(any(TestProject.class), eq(USER_ID))).thenReturn(savedEnvironment);

            // Act
            testProjectService.create(PROJECT_ID, validRequest, USER_ID);

            // Assert
            verify(environmentService).createDefault(savedProject, USER_ID);
        }

        @Test
        @DisplayName("should throw TestProjectLimitReachedException when project already has max TestProjects (RN10.01.2)")
        void shouldThrowExceptionWhenLimitReached() {
            // Arrange
            when(testProjectRepository.countByProjectId(PROJECT_ID)).thenReturn(2L);

            // Act & Assert
            assertThatThrownBy(() -> testProjectService.create(PROJECT_ID, validRequest, USER_ID))
                    .isInstanceOf(TestProjectLimitReachedException.class)
                    .hasMessageContaining(PROJECT_ID.toString());

            verify(testProjectRepository, never()).save(any());
            verify(environmentService, never()).createDefault(any(), any());
        }

        @Test
        @DisplayName("should throw TestProjectNameAlreadyExistsException when name already exists (RN10.01.3)")
        void shouldThrowExceptionWhenNameAlreadyExists() {
            // Arrange
            when(testProjectRepository.countByProjectId(PROJECT_ID)).thenReturn(0L);

            TestProject existingWithSameName = createTestProject(1L, validRequest.name(), null);
            when(testProjectRepository.findByProjectIdAndName(PROJECT_ID, validRequest.name()))
                    .thenReturn(Optional.of(existingWithSameName));

            // Act & Assert
            assertThatThrownBy(() -> testProjectService.create(PROJECT_ID, validRequest, USER_ID))
                    .isInstanceOf(TestProjectNameAlreadyExistsException.class)
                    .hasMessageContaining(validRequest.name())
                    .hasMessageContaining(PROJECT_ID.toString());

            verify(testProjectRepository, never()).save(any());
            verify(environmentService, never()).createDefault(any(), any());
        }

        @Test
        @DisplayName("should create TestProject with null description")
        void shouldCreateTestProjectWithNullDescription() {
            // Arrange
            CreateTestProjectRequestDTO requestWithoutDescription = new CreateTestProjectRequestDTO(
                    "Simple Test Suite",
                    null
            );

            when(testProjectRepository.countByProjectId(PROJECT_ID)).thenReturn(0L);
            when(testProjectRepository.findByProjectIdAndName(PROJECT_ID, requestWithoutDescription.name()))
                    .thenReturn(Optional.empty());

            TestProject savedProject = createTestProject(500L, requestWithoutDescription.name(), null);
            when(testProjectRepository.save(any(TestProject.class))).thenReturn(savedProject);

            Environment savedEnvironment = createDefaultEnvironment(savedProject);
            when(environmentService.createDefault(any(TestProject.class), eq(USER_ID))).thenReturn(savedEnvironment);

            // Act
            TestProjectResponseDTO response = testProjectService.create(PROJECT_ID, requestWithoutDescription, USER_ID);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.description()).isNull();
        }

        private TestProject createTestProject(Long id, String name, String description) {
            return TestProject.builder()
                    .id(id)
                    .projectId(PROJECT_ID)
                    .name(name)
                    .description(description)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .createdBy(USER_ID)
                    .lastUpdatedBy(USER_ID)
                    .build();
        }

        private Environment createDefaultEnvironment(TestProject testProject) {
            return Environment.builder()
                    .id(1L)
                    .testProject(testProject)
                    .name("Default")
                    .description("Default environment created automatically")
                    .isDefault(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .createdBy(USER_ID)
                    .lastUpdatedBy(USER_ID)
                    .build();
        }
    }
}

