package com.aegis.homolog.orchestrator.repository;

import com.aegis.homolog.orchestrator.model.entity.ApiCall;
import com.aegis.homolog.orchestrator.model.entity.Domain;
import com.aegis.homolog.orchestrator.model.entity.Tag;
import com.aegis.homolog.orchestrator.model.entity.TestProject;
import com.aegis.homolog.orchestrator.model.entity.TestScenario;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class TestEntityFactory {

    private TestEntityFactory() {
    }

    static TestProject testProject(String projectId, String scope) {
        return TestProject.builder()
                .projectId(projectId)
                .teamId("team-" + projectId)
                .name("Regression Suite " + projectId)
                .scope(scope)
                .description("Regression coverage for " + scope)
            .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
            .updatedAt(Instant.parse("2024-01-02T00:00:00Z"))
            .createdBy("AutoAI")
            .lastUpdatedBy("AutoAI")
                .build();
    }

    static Domain domain(String domainId, String name) {
        return Domain.builder()
                .domainId(domainId)
                .name(name)
                .description(name + " critical domain")
            .createdAt(Instant.parse("2024-01-03T00:00:00Z"))
            .updatedAt(Instant.parse("2024-01-04T00:00:00Z"))
            .createdBy("AutoAI")
            .lastUpdatedBy("AutoAI")
                .build();
    }

    static Tag tag(String tagId, String level) {
        return Tag.builder()
                .tagId(tagId)
                .name(tagId.equals("tag-reg") ? "Regression" : "Smoke")
                .description("Tag " + tagId)
                .level(level)
            .createdAt(Instant.parse("2024-01-05T00:00:00Z"))
            .updatedAt(Instant.parse("2024-01-06T00:00:00Z"))
            .createdBy("AutoAI")
            .lastUpdatedBy("AutoAI")
                .build();
    }

    static ApiCall apiCall(String callId, TestProject project, Domain domain) {
        return ApiCall.builder()
                .callId(callId)
                .project(project)
                .domain(domain)
                .baseUrlId("base-hml")
                .routeDefinition("/v1/payments")
                .method("POST")
                .baseGherkin("Given a payload when calling payments")
                .customVariables(Set.of("AUTH_TOKEN"))
                .requiredParams("{\"amount\":100}")
                .createdBy("AutoAI")
                .lastUpdatedBy("AutoAI")
                .build();
    }

    static TestScenario testScenario(String scenarioId, TestProject project, List<String> tagIds) {
        return TestScenario.builder()
                .scenarioId(scenarioId)
                .featureId("feature-" + scenarioId)
                .project(project)
                .title("Create entity " + scenarioId)
                .tagIds(new ArrayList<>(tagIds))
                .customVariableIds(List.of("var-auth"))
                .usedApiCallIds(List.of("call-create"))
                .abstractModel("{ \"nodes\": [] }")
                .generatedGherkin("Feature: Sample\nScenario: Create")
                .createdBy("AutoAI")
                .lastUpdatedBy("AutoAI")
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2024-01-02T00:00:00Z"))
                .totalExecutions(10)
                .totalFailures(0)
                .failureRate(BigDecimal.ZERO)
                .build();
    }
}
