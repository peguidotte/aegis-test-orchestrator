package com.aegis.tests.orchestrator.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Represents a reusable base URL for API calls.
 * <p>
 * Base URLs are environment-specific and can be shared across multiple ApiCalls.
 * This allows easy switching between environments (dev, staging, prod) without
 * modifying individual endpoint definitions.
 */
@Entity
@Table(
        name = "t_aegis_base_urls",
        indexes = {
                @Index(name = "idx_base_url_project_id", columnList = "test_project_id"),
                @Index(name = "idx_base_url_environment_id", columnList = "environment_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_base_url_project_env_identifier",
                        columnNames = {"test_project_id", "environment_id", "identifier"}
                )
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"testProject", "environment"})
public class BaseUrl extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_project_id", nullable = false)
    private TestProject testProject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "environment_id", nullable = false)
    private Environment environment;

    /**
     * Unique identifier for this base URL within the project/environment.
     * Example: "INVOICES_API", "AUTH_SERVICE", "PAYMENTS_GATEWAY"
     */
    @Column(name = "identifier", nullable = false, length = 100)
    private String identifier;

    /**
     * The complete base URL.
     * Example: "https://api-dev.example.com/v1", "http://localhost:8080"
     */
    @Column(name = "url", nullable = false, length = 500)
    private String url;

    /**
     * Optional description of what this base URL is for.
     */
    @Column(name = "description", length = 500)
    private String description;
}
