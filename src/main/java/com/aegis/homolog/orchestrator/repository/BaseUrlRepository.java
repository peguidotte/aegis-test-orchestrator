package com.aegis.homolog.orchestrator.repository;

import com.aegis.homolog.orchestrator.model.entity.BaseUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for BaseUrl entities.
 */
public interface BaseUrlRepository extends JpaRepository<BaseUrl, Long> {

    /**
     * Checks if a BaseUrl exists with the given identifier within a project and environment.
     */
    boolean existsByTestProjectIdAndEnvironmentIdAndIdentifier(
            Long testProjectId, Long environmentId, String identifier);

    /**
     * Finds a BaseUrl by its identifier within a project and environment.
     */
    Optional<BaseUrl> findByTestProjectIdAndEnvironmentIdAndIdentifier(
            Long testProjectId, Long environmentId, String identifier);

    /**
     * Finds all BaseUrls for a given project.
     */
    List<BaseUrl> findByTestProjectId(Long testProjectId);

    /**
     * Finds all BaseUrls for a given environment.
     */
    List<BaseUrl> findByEnvironmentId(Long environmentId);

    /**
     * Finds all BaseUrls for a given project and environment.
     */
    List<BaseUrl> findByTestProjectIdAndEnvironmentId(Long testProjectId, Long environmentId);
}
