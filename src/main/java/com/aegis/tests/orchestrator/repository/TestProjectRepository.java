package com.aegis.tests.orchestrator.repository;

import com.aegis.tests.orchestrator.model.entity.TestProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestProjectRepository extends JpaRepository<TestProject, Long> {

    List<TestProject> findByProjectId(Long projectId);

    Optional<TestProject> findByProjectIdAndName(Long projectId, String name);

    long countByProjectId(Long projectId);
}
