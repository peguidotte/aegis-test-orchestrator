package com.aegis.homolog.orchestrator.services;

import com.aegis.homolog.orchestrator.exception.TestProjectLimitReachedException;
import com.aegis.homolog.orchestrator.exception.TestProjectNameAlreadyExistsException;
import com.aegis.homolog.orchestrator.model.dto.CreateTestProjectRequestDTO;
import com.aegis.homolog.orchestrator.model.dto.TestProjectResponseDTO;
import com.aegis.homolog.orchestrator.model.entity.TestProject;
import com.aegis.homolog.orchestrator.repository.TestProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestProjectService {

    private static final int MAX_TEST_PROJECTS_PER_PROJECT = 2; // MVP limit

    private final TestProjectRepository testProjectRepository;
    private final EnvironmentService environmentService;

    public TestProjectService(TestProjectRepository testProjectRepository,
                              EnvironmentService environmentService) {
        this.testProjectRepository = testProjectRepository;
        this.environmentService = environmentService;
    }

    /**
     * Creates a new TestProject for the specified project.
     *
     * @param projectId Core Project ID
     * @param request DTO with TestProject data
     * @param userId ID of the user creating the resource
     * @return DTO with created TestProject data
     * @throws TestProjectLimitReachedException if TestProject limit is reached
     * @throws TestProjectNameAlreadyExistsException if a TestProject with same name exists
     */
    @Transactional
    public TestProjectResponseDTO create(Long projectId, CreateTestProjectRequestDTO request, String userId) {
        validateTestProjectLimit(projectId);
        validateNameUniqueness(projectId, request.name());

        TestProject testProject = buildTestProject(projectId, request, userId);
        TestProject savedProject = testProjectRepository.save(testProject);

        environmentService.createDefault(savedProject, userId);

        return TestProjectResponseDTO.fromEntity(savedProject);
    }

    private void validateTestProjectLimit(Long projectId) {
        long count = testProjectRepository.countByProjectId(projectId);
        if (count >= MAX_TEST_PROJECTS_PER_PROJECT) {
            throw new TestProjectLimitReachedException(projectId);
        }
    }

    private void validateNameUniqueness(Long projectId, String name) {
        testProjectRepository.findByProjectIdAndName(projectId, name)
                .ifPresent(existing -> {
                    throw new TestProjectNameAlreadyExistsException(name, projectId);
                });
    }

    private TestProject buildTestProject(Long projectId, CreateTestProjectRequestDTO request, String userId) {
        return TestProject.builder()
                .projectId(projectId)
                .name(request.name())
                .description(request.description())
                .createdBy(userId)
                .lastUpdatedBy(userId)
                .build();
    }
}

