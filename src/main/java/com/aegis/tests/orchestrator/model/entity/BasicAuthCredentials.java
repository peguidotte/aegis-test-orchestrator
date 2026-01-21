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

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Entity
@DiscriminatorValue("BASIC_AUTH")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"username", "password"})
public class BasicAuthCredentials extends AuthCredentials {

    @Sensitive
    @Column(name = "username", length = 512)
    private String username;

    @Sensitive
    @Column(name = "password", length = 512)
    @Convert(converter = EncryptedStringConverter.class)
    private String password;

    @Override
    public String buildAuthorizationHeader() {
        validate();
        String credentials = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8)
        );
        return "Basic " + encoded;
    }

    @Override
    public String getAuthType() {
        return "BASIC_AUTH";
    }

    @Override
    public void validate() {
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Username is required for Basic Auth authentication");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Password is required for Basic Auth authentication");
        }
    }
}


