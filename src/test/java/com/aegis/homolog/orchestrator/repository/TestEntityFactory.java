package com.aegis.homolog.orchestrator.repository;

import com.aegis.homolog.orchestrator.model.entity.ApiCall;
import com.aegis.homolog.orchestrator.model.entity.BaseUrl;
import com.aegis.homolog.orchestrator.model.entity.Domain;
import com.aegis.homolog.orchestrator.model.entity.Environment;
import com.aegis.homolog.orchestrator.model.entity.Tag;
import com.aegis.homolog.orchestrator.model.entity.TestProject;
import com.aegis.homolog.orchestrator.model.entity.TestScenario;
import com.aegis.homolog.orchestrator.model.enums.HttpMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class TestEntityFactory {

    private TestEntityFactory() {
    }

    static TestProject testProject(Long projectId, String scope) {
        Instant now = Instant.now();
        return TestProject.builder()
                .projectId(projectId)
                .name("Regression Suite " + projectId)
                .description("Regression coverage for " + scope)
                .createdAt(now.minus(30, ChronoUnit.DAYS))
                .updatedAt(now.minus(29, ChronoUnit.DAYS))
                .createdBy("AutoAI")
                .lastUpdatedBy("AutoAI")
                .build();
    }

    static Environment environment(TestProject testProject, String name) {
        Instant now = Instant.now();
        return Environment.builder()
                .testProject(testProject)
                .name(name)
                .description("Environment " + name)
                .isDefault("DEV".equals(name))
                .createdAt(now.minus(29, ChronoUnit.DAYS))
                .updatedAt(now.minus(28, ChronoUnit.DAYS))
                .createdBy("AutoAI")
                .lastUpdatedBy("AutoAI")
                .build();
    }

    static BaseUrl baseUrl(TestProject testProject, Environment environment, String identifier) {
        Instant now = Instant.now();
        return BaseUrl.builder()
                .testProject(testProject)
                .environment(environment)
                .identifier(identifier)
                .url("https://api.example.com/v1")
                .description("Base URL for " + identifier)
                .createdAt(now.minus(27, ChronoUnit.DAYS))
                .updatedAt(now.minus(26, ChronoUnit.DAYS))
                .createdBy("AutoAI")
                .lastUpdatedBy("AutoAI")
                .build();
    }

    static Domain domain(String name) {
        Instant now = Instant.now();
        return Domain.builder()
                .name(name)
                .description(name + " critical domain")
                .createdAt(now.minus(28, ChronoUnit.DAYS))
                .updatedAt(now.minus(27, ChronoUnit.DAYS))
                .createdBy("AutoAI")
                .lastUpdatedBy("AutoAI")
                .build();
    }

    static Tag tag(String name, String level) {
        Instant now = Instant.now();
        return Tag.builder()
                .name(name)
                .description("Tag " + name)
                .level(level)
                .createdAt(now.minus(26, ChronoUnit.DAYS))
                .updatedAt(now.minus(25, ChronoUnit.DAYS))
                .createdBy("AutoAI")
                .lastUpdatedBy("AutoAI")
                .build();
    }

    static ApiCall apiCall(TestProject project, Domain domain, BaseUrl baseUrl) {
        Instant now = Instant.now();
        return ApiCall.builder()
                .project(project)
                .domain(domain)
                .baseUrl(baseUrl)
                .routeDefinition("/v1/payments")
                .method(HttpMethod.POST)
                .description("Creates a new payment")
                .requestExample("{\"amount\": 100}")
                .responseExample("{\"id\": 1, \"status\": \"CREATED\"}")
                .baseGherkin("Given a payload when calling payments")
                .customVariables(Set.of("AUTH_TOKEN"))
                .requiredParams("{\"amount\":100}")
                .requiresAuth(true)
                .createdAt(now.minus(24, ChronoUnit.DAYS))
                .updatedAt(now.minus(23, ChronoUnit.DAYS))
                .createdBy("AutoAI")
                .lastUpdatedBy("AutoAI")
                .build();
    }

    static TestScenario testScenario(TestProject project, List<Long> tagIds) {
        Instant now = Instant.now();
        return TestScenario.builder()
                .featureId("feature-sample")
                .project(project)
                .title("Create entity scenario")
                .tagIds(new ArrayList<>(tagIds))
                .customVariableIds(List.of("var-auth"))
                .usedApiCallIds(new ArrayList<>())
                .abstractModel("{ \"nodes\": [] }")
                .generatedGherkin("Feature: Sample\nScenario: Create")
                .createdBy("AutoAI")
                .lastUpdatedBy("AutoAI")
                .createdAt(now.minus(20, ChronoUnit.DAYS))
                .updatedAt(now.minus(19, ChronoUnit.DAYS))
                .totalExecutions(10)
                .totalFailures(0)
                .failureRate(BigDecimal.ZERO)
                .build();
    }
}
