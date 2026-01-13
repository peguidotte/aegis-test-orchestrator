package com.aegis.homolog.orchestrator.model.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@ToString(callSuper = true, exclude = {"project", "domain"})
@Entity
@Table(name = "t_aegis_api_calls")
public class ApiCall extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private TestProject project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "domain_id", nullable = false)
    private Domain domain;

    @Column(name = "base_url_id", nullable = false, length = 64)
    private String baseUrlId;

    @Column(name = "route_definition", nullable = false, length = 256)
    private String routeDefinition;

    @Column(name = "http_method", nullable = false, length = 16)
    private String method;

    @ElementCollection
    @CollectionTable(name = "t_aegis_api_call_vars", joinColumns = @JoinColumn(name = "api_call_id"))
    @Column(name = "variable_id", length = 128)
    @Builder.Default
    private Set<String> customVariables = new HashSet<>();

    @Lob
    @Column(name = "base_gherkin", nullable = false)
    private String baseGherkin;

    @Lob
    @Column(name = "required_params")
    private String requiredParams;

}
