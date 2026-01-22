package com.aegis.tests.orchestrator.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for API documentation.
 *
 * Documentation available at:
 * - Redoc: http://localhost:8080/redoc.html (recommended - cleaner UI)
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Aegis Test Orchestrator API")
                        .version("1.0.0")
                        .description("""
                                # Aegis Test Orchestrator
                                
                                API for orchestrating AI-driven automated test generation and execution.
                                
                                ## Features
                                - **Test Projects**: Organize your test suites
                                - **Specifications**: Define what to test with AI assistance
                                - **Environments**: Manage different test environments
                                - **API Catalog**: Reusable endpoint definitions
                                - **Authentication Profiles**: Manage test credentials
                                
                                ## Getting Started
                                1. Create a TestProject
                                2. Define Specifications (MANUAL or API_CALL mode)
                                3. Let the AI generate comprehensive test scenarios
                                
                                ## Authentication
                                Currently in MVP mode - authentication is disabled.
                                JWT authentication will be required in production.
                                """)
                        .contact(new Contact()
                                .name("Aegis Team")
                                .email("aegis@example.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://aegis.example.com/license")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("https://api-staging.aegis.example.com").description("Staging"),
                        new Server().url("https://api.aegis.example.com").description("Production")
                ));
    }
}

