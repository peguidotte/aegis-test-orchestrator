package com.aegis.homolog.orchestrator.model.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "T_AEGIS_API_CALLS")
public class ApiCall extends AuditableEntity {

    @Id
    @Column(name = "CALL_ID", nullable = false, length = 64)
    private String callId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private TestProject project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DOMAIN_ID", nullable = false)
    private Domain domain;

    @Column(name = "BASE_URL_ID", nullable = false, length = 64)
    private String baseUrlId;

    @Column(name = "ROUTE_DEFINITION", nullable = false, length = 256)
    private String routeDefinition;

    @Column(name = "HTTP_METHOD", nullable = false, length = 16)
    private String method;

    @ElementCollection
    @CollectionTable(name = "T_AEGIS_API_CALL_VARS", joinColumns = @JoinColumn(name = "CALL_ID"))
    @Column(name = "VARIABLE_ID", length = 128)
    @Builder.Default
    private Set<String> customVariables = new HashSet<>();

    @Lob
    @Column(name = "BASE_GHERKIN", nullable = false)
    private String baseGherkin;

    @Lob
    @Column(name = "REQUIRED_PARAMS")
    private String requiredParams;

}
