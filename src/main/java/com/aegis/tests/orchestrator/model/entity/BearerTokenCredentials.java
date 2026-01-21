package com.aegis.tests.orchestrator.model.entity;

import com.aegis.tests.orchestrator.config.EncryptedStringConverter;
import com.aegis.tests.orchestrator.config.Sensitive;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("BEARER_TOKEN")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"token"})
public class BearerTokenCredentials extends AuthCredentials {

    @Sensitive
    @Column(name = "token", columnDefinition = "TEXT")
    @Convert(converter = EncryptedStringConverter.class)
    private String token;

    @Override
    public String buildAuthorizationHeader() {
        validate();
        return "Bearer " + token;
    }

    @Override
    public String getAuthType() {
        return "BEARER_TOKEN";
    }

    @Override
    public void validate() {
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Token is required for Bearer Token authentication");
        }
    }
}

