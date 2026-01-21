package com.aegis.tests.orchestrator.services;

import com.aegis.tests.orchestrator.model.entity.Environment;
import com.aegis.tests.orchestrator.model.entity.TestProject;
import com.aegis.tests.orchestrator.repository.EnvironmentRepository;
import org.springframework.stereotype.Service;

/**
 * Service responsible for Environment domain operations.
 */
@Service
public class EnvironmentService {

    private static final String DEFAULT_ENVIRONMENT_NAME = "Default";
    private static final String DEFAULT_ENVIRONMENT_DESCRIPTION = "Default environment created automatically";

    private final EnvironmentRepository environmentRepository;

    public EnvironmentService(EnvironmentRepository environmentRepository) {
        this.environmentRepository = environmentRepository;
    }

    /**
     * Creates the default environment for a TestProject.
     * Called automatically when a TestProject is created.
     *
     * @param testProject The TestProject to create the default environment for
     * @param userId The user creating the environment
     * @return The created default Environment
     */
    public Environment createDefault(TestProject testProject, String userId) {
        Environment defaultEnvironment = Environment.builder()
                .testProject(testProject)
                .name(DEFAULT_ENVIRONMENT_NAME)
                .description(DEFAULT_ENVIRONMENT_DESCRIPTION)
                .isDefault(true)
                .createdBy(userId)
                .lastUpdatedBy(userId)
                .build();

        return environmentRepository.save(defaultEnvironment);
    }
}

