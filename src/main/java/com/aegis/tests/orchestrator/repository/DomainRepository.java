package com.aegis.tests.orchestrator.repository;

import com.aegis.tests.orchestrator.model.entity.Domain;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainRepository extends JpaRepository<Domain, Long> {

    List<Domain> findByNameContainingIgnoreCase(String fragment);
}
