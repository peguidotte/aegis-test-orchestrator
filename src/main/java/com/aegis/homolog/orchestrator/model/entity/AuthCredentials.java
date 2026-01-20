package com.aegis.homolog.orchestrator.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "t_aegis_auth_credentials")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "auth_type", discriminatorType = DiscriminatorType.STRING, length = 30)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class AuthCredentials {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "credentials_id", nullable = false)
    private Long id;

    /**
     * Builds the Authorization header value for this credential type.
     * Example: "Bearer abc123" or "Basic dXNlcjpwYXNz"
     *
     * @return the complete Authorization header value
     */
    public abstract String buildAuthorizationHeader();

    /**
     * Returns the authentication type identifier.
     * Used for API responses and logging.
     *
     * @return the auth type name (e.g., "BEARER_TOKEN", "BASIC_AUTH")
     */
    public abstract String getAuthType();

    /**
     * Validates that all required fields for this credential type are present.
     *
     * @throws IllegalStateException if validation fails
     */
    public abstract void validate();
}

