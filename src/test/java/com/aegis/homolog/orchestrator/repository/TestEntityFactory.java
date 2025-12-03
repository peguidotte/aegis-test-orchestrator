package com.aegis.homolog.orchestrator.repository;

import com.aegis.homolog.orchestrator.model.entity.ApiCall;
import com.aegis.homolog.orchestrator.model.entity.Domain;
import com.aegis.homolog.orchestrator.model.entity.Tag;
import com.aegis.homolog.orchestrator.model.entity.TestProject;
import com.aegis.homolog.orchestrator.model.entity.TestScenario;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class TestEntityFactory {

    private TestEntityFactory() {
    }

    static TestProject testProject(String projectId, String scope) {
        Instant now = Instant.now();
        return TestProject.builder()
                .projectId(projectId)
                .teamId("team-" + projectId)
                .name("Regression Suite " + projectId)
                .scope(scope)
                .description("Regression coverage for " + scope)
            .createdAt(now.minus(30, ChronoUnit.DAYS))
            .updatedAt(now.minus(29, ChronoUnit.DAYS))
            .createdBy("AutoAI")
            .lastUpdatedBy("AutoAI")
                .build();
    }

    static Domain domain(String domainId, String name) {
        Instant now = Instant.now();
        return Domain.builder()
                .domainId(domainId)
                .name(name)
                .description(name + " critical domain")
            .createdAt(now.minus(28, ChronoUnit.DAYS))
            .updatedAt(now.minus(27, ChronoUnit.DAYS))
            .createdBy("AutoAI")
            .lastUpdatedBy("AutoAI")
                .build();
    }

    static Tag tag(String tagId, String level) {
        Instant now = Instant.now();
        return Tag.builder()
                .tagId(tagId)
                .name(tagId.equals("tag-reg") ? "Regression" : "Smoke")
                .description("Tag " + tagId)
                .level(level)
            .createdAt(now.minus(26, ChronoUnit.DAYS))
            .updatedAt(now.minus(25, ChronoUnit.DAYS))
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
        Instant now = Instant.now();
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
                .createdAt(now.minus(20, ChronoUnit.DAYS))
                .updatedAt(now.minus(19, ChronoUnit.DAYS))
                .totalExecutions(10)
                .totalFailures(0)
                .failureRate(BigDecimal.ZERO)
                .build();
    }
}
