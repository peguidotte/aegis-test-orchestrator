package com.aegis.homolog.orchestrator.repository;

import com.aegis.homolog.orchestrator.model.entity.Specification;
import com.aegis.homolog.orchestrator.model.enums.HttpMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecificationRepository extends JpaRepository<Specification, Long> {
    boolean existsByMethodAndPathAndEnvironmentId(HttpMethod method, String path, Long environmentId);
}

