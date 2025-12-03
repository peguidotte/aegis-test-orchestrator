package com.aegis.homolog.orchestrator.repository;

import com.aegis.homolog.orchestrator.model.entity.Tag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, String> {

    List<Tag> findByLevel(String level);
}
