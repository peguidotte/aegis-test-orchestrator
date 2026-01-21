package com.aegis.tests.orchestrator.repository;

import com.aegis.tests.orchestrator.model.entity.AuthProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthProfileRepository extends JpaRepository<AuthProfile, Long> {
    boolean existsByIdAndEnvironmentId(Long id, Long environmentId);
}

