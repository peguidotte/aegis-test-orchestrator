package com.aegis.homolog.orchestrator.repository;

import com.aegis.homolog.orchestrator.model.entity.TestScenario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestScenarioRepository extends JpaRepository<TestScenario, String> {

    List<TestScenario> findByProjectProjectIdAndTagIdsContaining(String projectId, String tagId);
}
