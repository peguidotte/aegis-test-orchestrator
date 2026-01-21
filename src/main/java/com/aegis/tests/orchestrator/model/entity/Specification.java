package com.aegis.tests.orchestrator.model.entity;

import com.aegis.tests.orchestrator.model.enums.HttpMethod;
import com.aegis.tests.orchestrator.model.enums.SpecStatus;
import com.aegis.tests.orchestrator.model.enums.SpecificationInputType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a test specification for an API endpoint.
 * <p>
 * A Specification defines "what to test" for a specific endpoint and serves as
 * the functional source of truth for AI-driven test generation. It can be created
 * in two modalities:
 * <ul>
 *   <li>MANUAL: User provides method, path, and request example directly</li>
 *   <li>API_CALL: User references an existing ApiCall from the catalog</li>
 * </ul>
 */
@Entity
@Table(
        name = "t_aegis_specifications",
        indexes = {
                @Index(name = "idx_specification_test_project_id", columnList = "test_project_id"),
                @Index(name = "idx_specification_environment_id", columnList = "environment_id"),
                @Index(name = "idx_specification_status", columnList = "status"),
                @Index(name = "idx_specification_input_type", columnList = "input_type")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_specification_method_path_env",
                        columnNames = {"method", "path", "environment_id"}
                )
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"testProject", "environment", "domain", "authProfile", "apiCall", "supportingApiCalls"})
public class Specification extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "specification_id", nullable = false)
    private Long id;

    /**
     * The input modality used to create this specification.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "input_type", nullable = false, length = 20)
    private SpecificationInputType inputType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_project_id", nullable = false)
    private TestProject testProject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "environment_id", nullable = false)
    private Environment environment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id")
    private Domain domain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auth_profile_id")
    private AuthProfile authProfile;

    /**
     * Reference to an ApiCall when inputType is API_CALL.
     * Null when inputType is MANUAL.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_call_id")
    private ApiCall apiCall;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    /**
     * Detailed test objective describing what should be tested.
     * This is the main context for the AI to understand the testing goals.
     */
    @Column(name = "test_objective", nullable = false, length = 2000)
    private String testObjective;

    /**
     * HTTP method for the endpoint.
     * Resolved from ApiCall when inputType is API_CALL.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 10)
    private HttpMethod method;

    /**
     * API path for the endpoint.
     * Resolved from ApiCall when inputType is API_CALL.
     */
    @Column(name = "path", nullable = false, length = 500)
    private String path;

    @Column(name = "requires_auth", nullable = false)
    private Boolean requiresAuth;

    @Column(name = "request_example", columnDefinition = "TEXT")
    private String requestExample;

    @Column(name = "approve_before_generation", nullable = false)
    private Boolean approveBeforeGeneration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SpecStatus status;

    /**
     * Additional ApiCalls that provide context for the AI.
     * These endpoints can be used for setup, teardown, or validation.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "t_aegis_specification_supporting_api_calls",
            joinColumns = @JoinColumn(name = "specification_id"),
            inverseJoinColumns = @JoinColumn(name = "api_call_id")
    )
    @Builder.Default
    private Set<ApiCall> supportingApiCalls = new HashSet<>();
}

