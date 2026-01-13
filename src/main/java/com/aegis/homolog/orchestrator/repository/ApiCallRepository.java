package com.aegis.homolog.orchestrator.repository;

import com.aegis.homolog.orchestrator.model.entity.ApiCall;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiCallRepository extends JpaRepository<ApiCall, Long> {

    List<ApiCall> findByProjectIdAndDomainId(Long projectId, Long domainId);
}
