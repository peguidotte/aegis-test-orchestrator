package com.aegis.homolog.orchestrator.repository;

import com.aegis.homolog.orchestrator.model.entity.TestProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestProjectRepository extends JpaRepository<TestProject, Long> {

    List<TestProject> findByProjectId(Long projectId);

    Optional<TestProject> findByProjectIdAndName(Long projectId, String name);

    long countByProjectId(Long projectId);
}
