package com.aegis.tests.orchestrator.model.entity;

import com.aegis.tests.orchestrator.model.enums.HttpMethod;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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

/**
 * Represents a cataloged API endpoint.
 * <p>
 * ApiCalls serve as a reusable registry of endpoints that can be referenced
 * when creating Specifications. This avoids duplication of endpoint information
 * and allows for consistent endpoint definitions across test specifications.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"project", "domain", "baseUrl"})
@Entity
@Table(
        name = "t_aegis_api_calls",
        indexes = {
                @Index(name = "idx_api_call_project_id", columnList = "project_id"),
                @Index(name = "idx_api_call_domain_id", columnList = "domain_id"),
                @Index(name = "idx_api_call_base_url_id", columnList = "base_url_id")
        }
)
public class ApiCall extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private TestProject project;

    /**
     * Domain is optional - can be assigned later for semantic grouping.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id")
    private Domain domain;

    /**
     * Reference to the base URL for this endpoint.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "base_url_id", nullable = false)
    private BaseUrl baseUrl;

    /**
     * The route/path definition for this endpoint.
     * Example: "/v1/invoices", "/v1/invoices/{invoiceId}"
     */
    @Column(name = "route_definition", nullable = false, length = 256)
    private String routeDefinition;

    /**
     * HTTP method for this endpoint.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "http_method", nullable = false, length = 16)
    private HttpMethod method;

    /**
     * Brief description of what this endpoint does.
     * Example: "Creates a new invoice", "Retrieves invoice by ID"
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * JSON example of a typical request payload.
     */
    @Lob
    @Column(name = "request_example")
    private String requestExample;

    /**
     * JSON example of a typical response payload.
     */
    @Lob
    @Column(name = "response_example")
    private String responseExample;

    @ElementCollection
    @CollectionTable(name = "t_aegis_api_call_vars", joinColumns = @JoinColumn(name = "api_call_id"))
    @Column(name = "variable_id", length = 128)
    @Builder.Default
    private Set<String> customVariables = new HashSet<>();

    /**
     * Base KarateDSL template for this endpoint.
     */
    @Lob
    @Column(name = "base_gherkin")
    private String baseGherkin;

    /**
     * JSON describing required parameters for this endpoint.
     */
    @Lob
    @Column(name = "required_params")
    private String requiredParams;

    /**
     * Indicates if this endpoint requires authentication.
     */
    @Column(name = "requires_auth")
    @Builder.Default
    private Boolean requiresAuth = true;
}
