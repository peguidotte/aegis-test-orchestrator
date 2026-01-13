package com.aegis.homolog.orchestrator.repository;

import com.aegis.homolog.orchestrator.model.entity.TestProject;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestProjectRepository extends JpaRepository<TestProject, Long> {

    List<TestProject> findByScope(String scope);
}
