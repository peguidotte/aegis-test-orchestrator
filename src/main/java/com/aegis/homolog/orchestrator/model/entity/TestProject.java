package com.aegis.homolog.orchestrator.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Entity
@Table(name = "T_AEGIS_PROJECTS")
public class TestProject extends AuditableEntity {

    @Id
    @Column(name = "PROJECT_ID", nullable = false, length = 64)
    private String projectId;

    @Column(name = "TEAM_ID", nullable = false, length = 64)
    private String teamId;

    @Column(name = "NAME", nullable = false, length = 128)
    private String name;

    @Column(name = "SCOPE", nullable = false, length = 64)
    private String scope;

    @Column(name = "DESCRIPTION", length = 1024)
    private String description;
}
