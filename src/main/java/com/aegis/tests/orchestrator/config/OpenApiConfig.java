package com.aegis.tests.orchestrator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do OpenAPI/Swagger para documentação da API.
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
                                API module of Aegis Tests for orchestrating automated tests.
                                """)
                        .contact(new Contact()
                                .name("Aegis Team")
                                .email("aegis@example.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://aegis.example.com/license")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development")
                ));
    }
}

