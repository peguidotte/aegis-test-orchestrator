package com.aegis.tests.orchestrator.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AuthCredentials Hierarchy")
class AuthCredentialsTest {

    @Nested
    @DisplayName("BearerTokenCredentials")
    class BearerTokenCredentialsTests {

        @Test
        @DisplayName("should build correct Authorization header")
        void shouldBuildCorrectAuthorizationHeader() {
            // Arrange
            var credentials = BearerTokenCredentials.builder()
                    .token("my-secret-token")
                    .build();

            // Act
            String header = credentials.buildAuthorizationHeader();

            // Assert
            assertThat(header).isEqualTo("Bearer my-secret-token");
        }

        @Test
        @DisplayName("should return correct auth type")
        void shouldReturnCorrectAuthType() {
            // Arrange
            var credentials = BearerTokenCredentials.builder()
                    .token("token")
                    .build();

            // Act & Assert
            assertThat(credentials.getAuthType()).isEqualTo("BEARER_TOKEN");
        }

        @Test
        @DisplayName("should throw exception when token is null")
        void shouldThrowExceptionWhenTokenIsNull() {
            // Arrange
            var credentials = BearerTokenCredentials.builder()
                    .token(null)
                    .build();

            // Act & Assert
            assertThatThrownBy(credentials::buildAuthorizationHeader)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Token is required");
        }

        @Test
        @DisplayName("should throw exception when token is blank")
        void shouldThrowExceptionWhenTokenIsBlank() {
            // Arrange
            var credentials = BearerTokenCredentials.builder()
                    .token("   ")
                    .build();

            // Act & Assert
            assertThatThrownBy(credentials::buildAuthorizationHeader)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Token is required");
        }
    }

    @Nested
    @DisplayName("BasicAuthCredentials")
    class BasicAuthCredentialsTests {

        @Test
        @DisplayName("should build correct Authorization header with Base64 encoding")
        void shouldBuildCorrectAuthorizationHeader() {
            // Arrange
            var credentials = BasicAuthCredentials.builder()
                    .username("user")
                    .password("pass")
                    .build();

            // Act
            String header = credentials.buildAuthorizationHeader();

            // Assert
            String expectedEncoded = Base64.getEncoder().encodeToString(
                    "user:pass".getBytes(StandardCharsets.UTF_8)
            );
            assertThat(header)
                    .isEqualTo("Basic " + expectedEncoded)
                    .isEqualTo("Basic dXNlcjpwYXNz");
        }

        @Test
        @DisplayName("should return correct auth type")
        void shouldReturnCorrectAuthType() {
            // Arrange
            var credentials = BasicAuthCredentials.builder()
                    .username("user")
                    .password("pass")
                    .build();

            // Act & Assert
            assertThat(credentials.getAuthType()).isEqualTo("BASIC_AUTH");
        }

        @Test
        @DisplayName("should throw exception when username is null")
        void shouldThrowExceptionWhenUsernameIsNull() {
            // Arrange
            var credentials = BasicAuthCredentials.builder()
                    .username(null)
                    .password("pass")
                    .build();

            // Act & Assert
            assertThatThrownBy(credentials::buildAuthorizationHeader)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Username is required");
        }

        @Test
        @DisplayName("should throw exception when password is null")
        void shouldThrowExceptionWhenPasswordIsNull() {
            // Arrange
            var credentials = BasicAuthCredentials.builder()
                    .username("user")
                    .password(null)
                    .build();

            // Act & Assert
            assertThatThrownBy(credentials::buildAuthorizationHeader)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Password is required");
        }

        @Test
        @DisplayName("should handle special characters in credentials")
        void shouldHandleSpecialCharacters() {
            // Arrange
            var credentials = BasicAuthCredentials.builder()
                    .username("user@domain.com")
                    .password("p@ss:w0rd!")
                    .build();

            // Act
            String header = credentials.buildAuthorizationHeader();

            // Assert
            String expectedEncoded = Base64.getEncoder().encodeToString(
                    "user@domain.com:p@ss:w0rd!".getBytes(StandardCharsets.UTF_8)
            );
            assertThat(header).isEqualTo("Basic " + expectedEncoded);
        }
    }

    @Nested
    @DisplayName("AuthProfile with Credentials")
    class AuthProfileWithCredentialsTests {

        @Test
        @DisplayName("should delegate buildAuthorizationHeader to credentials")
        void shouldDelegateBuildAuthorizationHeader() {
            // Arrange
            var credentials = BearerTokenCredentials.builder()
                    .token("my-token")
                    .build();

            var authProfile = AuthProfile.builder()
                    .name("Test Profile")
                    .credentials(credentials)
                    .build();

            // Act
            String header = authProfile.buildAuthorizationHeader();

            // Assert
            assertThat(header).isEqualTo("Bearer my-token");
        }

        @Test
        @DisplayName("should return auth type from credentials")
        void shouldReturnAuthTypeFromCredentials() {
            // Arrange
            var credentials = BasicAuthCredentials.builder()
                    .username("user")
                    .password("pass")
                    .build();

            var authProfile = AuthProfile.builder()
                    .name("Test Profile")
                    .credentials(credentials)
                    .build();

            // Act & Assert
            assertThat(authProfile.getAuthType()).isEqualTo("BASIC_AUTH");
        }

        @Test
        @DisplayName("should throw exception when credentials is null")
        void shouldThrowExceptionWhenCredentialsIsNull() {
            // Arrange
            var authProfile = AuthProfile.builder()
                    .name("Test Profile")
                    .credentials(null)
                    .build();

            // Act & Assert
            assertThatThrownBy(authProfile::buildAuthorizationHeader)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No credentials configured");
        }
    }
}

