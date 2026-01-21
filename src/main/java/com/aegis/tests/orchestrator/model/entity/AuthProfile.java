package com.aegis.tests.orchestrator.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "t_aegis_auth_profiles",
        indexes = {
                @Index(name = "idx_auth_profile_environment_id", columnList = "environment_id"),
                @Index(name = "idx_auth_profile_name", columnList = "name")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_auth_profile_env_name", columnNames = {"environment_id", "name"})
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"environment", "credentials"})
public class AuthProfile extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_profile_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "environment_id", nullable = false)
    private Environment environment;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * The authentication credentials.
     * Can be
     * {@link BearerTokenCredentials}
     * {@link BasicAuthCredentials}.
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "credentials_id", nullable = false)
    private AuthCredentials credentials;

    public String buildAuthorizationHeader() {
        if (credentials == null) {
            throw new IllegalStateException("No credentials configured for AuthProfile: " + name);
        }
        return credentials.buildAuthorizationHeader();
    }

    public String getAuthType() {
        return credentials != null ? credentials.getAuthType() : null;
    }
}