package com.aegis.homolog.orchestrator.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_AEGIS_DOMAINS")
public class Domain {

    @Id
    @Column(name = "DOMAIN_ID", nullable = false, length = 64)
    private String domainId;

    @Column(name = "NAME", nullable = false, length = 128)
    private String name;

    @Column(name = "DESCRIPTION", length = 1024)
    private String description;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private Instant updatedAt;

    @Column(name = "CREATED_BY", nullable = false, length = 64)
    private String createdBy;

    @Column(name = "LAST_UPDATED_BY", nullable = false, length = 64)
    private String lastUpdatedBy;

    @PrePersist
    void onPersist() {
        var now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
