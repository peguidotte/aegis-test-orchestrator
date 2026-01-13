package com.aegis.homolog.orchestrator.repository;

import com.aegis.homolog.orchestrator.model.entity.Domain;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainRepository extends JpaRepository<Domain, Long> {

    List<Domain> findByNameContainingIgnoreCase(String fragment);
}
