package com.aegis.tests.orchestrator.repository;

import com.aegis.tests.orchestrator.model.entity.Tag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByLevel(String level);
}
