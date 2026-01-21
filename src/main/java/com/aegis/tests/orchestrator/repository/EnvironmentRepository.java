package com.aegis.tests.orchestrator.repository;

import com.aegis.tests.orchestrator.model.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnvironmentRepository extends JpaRepository<Environment, Long> {

    List<Environment> findByTestProjectId(Long testProjectId);

    Optional<Environment> findByTestProjectIdAndIsDefaultTrue(Long testProjectId);

    Optional<Environment> findByTestProjectIdAndName(Long testProjectId, String name);

    Optional<Environment> findByIdAndTestProjectId(Long id, Long testProjectId);
}

