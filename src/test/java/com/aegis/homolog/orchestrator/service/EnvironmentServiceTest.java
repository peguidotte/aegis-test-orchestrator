package com.aegis.homolog.orchestrator.service;

import com.aegis.homolog.orchestrator.model.entity.Environment;
import com.aegis.homolog.orchestrator.model.entity.TestProject;
import com.aegis.homolog.orchestrator.repository.EnvironmentRepository;
import com.aegis.homolog.orchestrator.services.EnvironmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnvironmentService")
class EnvironmentServiceTest {

    @Mock
    private EnvironmentRepository environmentRepository;

    @InjectMocks
    private EnvironmentService environmentService;

    @Captor
    private ArgumentCaptor<Environment> environmentCaptor;

    private static final String USER_ID = "user-123";

    @Test
    @DisplayName("createDefault should create environment with correct attributes and return saved entity")
    void createDefaultShouldCreateEnvironmentWithCorrectAttributes() {
        // Arrange
        TestProject testProject = createTestProject();
        Environment savedEnvironment = Environment.builder()
                .id(100L)
                .testProject(testProject)
                .name("Default")
                .description("Default environment created automatically")
                .isDefault(true)
                .createdBy(USER_ID)
                .lastUpdatedBy(USER_ID)
                .build();

        when(environmentRepository.save(any(Environment.class))).thenReturn(savedEnvironment);

        // Act
        Environment result = environmentService.createDefault(testProject, USER_ID);

        // Assert - verify what was saved
        verify(environmentRepository).save(environmentCaptor.capture());
        Environment captured = environmentCaptor.getValue();

        assertThat(captured.getName()).isEqualTo("Default");
        assertThat(captured.getDescription()).isEqualTo("Default environment created automatically");
        assertThat(captured.getIsDefault()).isTrue();
        assertThat(captured.getTestProject()).isEqualTo(testProject);
        assertThat(captured.getCreatedBy()).isEqualTo(USER_ID);
        assertThat(captured.getLastUpdatedBy()).isEqualTo(USER_ID);

        // Assert - verify return value
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
    }

    private TestProject createTestProject() {
        return TestProject.builder()
                .id(1L)
                .projectId(10L)
                .name("Test Project")
                .description("Test Project Description")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy(USER_ID)
                .lastUpdatedBy(USER_ID)
                .build();
    }
}

