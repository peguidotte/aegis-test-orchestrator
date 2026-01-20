package com.aegis.homolog.orchestrator.repository;

import com.aegis.homolog.orchestrator.model.entity.ApiCall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ApiCall entities.
 */
public interface ApiCallRepository extends JpaRepository<ApiCall, Long> {

    /**
     * Finds all ApiCalls for a given project and domain.
     */
    List<ApiCall> findByProjectIdAndDomainId(Long projectId, Long domainId);

    /**
     * Finds all ApiCalls for a given project.
     */
    List<ApiCall> findByProjectId(Long projectId);

    /**
     * Checks if an ApiCall exists and belongs to the specified project.
     */
    boolean existsByIdAndProjectId(Long id, Long projectId);

    /**
     * Finds an ApiCall by ID and project ID.
     */
    Optional<ApiCall> findByIdAndProjectId(Long id, Long projectId);

    /**
     * Finds all ApiCalls by IDs that belong to the specified project.
     * Used to validate supportingApiCallIds.
     */
    List<ApiCall> findAllByIdInAndProjectId(Collection<Long> ids, Long projectId);

    /**
     * Counts ApiCalls by IDs that belong to the specified project.
     * Used to validate if all provided IDs are valid.
     */
    long countByIdInAndProjectId(Collection<Long> ids, Long projectId);
}
