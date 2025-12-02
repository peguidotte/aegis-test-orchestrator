package com.aegis.homolog.orchestrator.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.aegis.homolog.orchestrator.model.entity.ApiCall;
import com.aegis.homolog.orchestrator.model.entity.Domain;
import com.aegis.homolog.orchestrator.model.entity.Tag;
import com.aegis.homolog.orchestrator.model.entity.TestScenario;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class RepositoryPersistenceTest {

    @Autowired
    private TestProjectRepository testProjectRepository;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ApiCallRepository apiCallRepository;

    @Autowired
    private TestScenarioRepository testScenarioRepository;

    @Test
    @DisplayName("Should store and query projects by scope")
    void shouldStoreAndQueryProjectsByScope() {
        var project = TestEntityFactory.testProject("proj-main", "internal_systems");
        testProjectRepository.save(project);

        var results = testProjectRepository.findByScope("internal_systems");

        assertThat(results)
                .hasSize(1)
                .first()
                .matches(saved -> saved.getProjectId().equals("proj-main")
                        && saved.getDescription().contains("Regression"));
    }

    @Test
    @DisplayName("Should fetch domains by name fragment")
    void shouldFetchDomainsByNameFragment() {
        var domain = TestEntityFactory.domain("domain-users", "Users");
        domainRepository.save(domain);

        var results = domainRepository.findByNameContainingIgnoreCase("user");

        assertThat(results)
                .hasSize(1)
                .first()
                .extracting(Domain::getDescription)
                .asString()
                .contains("critical");
    }

    @Test
    @DisplayName("Should filter tags by level")
    void shouldFilterTagsByLevel() {
        var regression = TestEntityFactory.tag("tag-reg", "scenario");
        var smoke = TestEntityFactory.tag("tag-smoke", "feature");
        tagRepository.saveAll(List.of(regression, smoke));

        var scenarioTags = tagRepository.findByLevel("scenario");

        assertThat(scenarioTags)
                .hasSize(1)
                .first()
                .extracting(Tag::getName)
                .isEqualTo("Regression");
    }

    @Test
    @DisplayName("Should join api calls by project and domain")
    void shouldJoinApiCallsByProjectAndDomain() {
        var project = testProjectRepository.save(TestEntityFactory.testProject("proj-api", "client_homologation"));
        var domain = domainRepository.save(TestEntityFactory.domain("domain-payments", "Payments"));

        var apiCall = TestEntityFactory.apiCall("call-create", project, domain);
        apiCallRepository.save(apiCall);

        var results = apiCallRepository.findByProjectProjectIdAndDomainDomainId("proj-api", "domain-payments");

        assertThat(results)
                .hasSize(1)
                .first()
                .extracting(ApiCall::getRouteDefinition)
                .isEqualTo("/v1/payments");
    }

    @Test
    @DisplayName("Should locate scenarios by tag and project")
    void shouldLocateScenariosByTagAndProject() {
        var project = testProjectRepository.save(TestEntityFactory.testProject("proj-scen", "internal_systems"));
        var scenario = TestEntityFactory.testScenario("scenario-create", project, List.of("tag-reg", "tag-bvt"));
        testScenarioRepository.save(scenario);

        var results = testScenarioRepository.findByProjectProjectIdAndTagIdsContaining("proj-scen", "tag-reg");

        assertThat(results)
            .hasSize(1)
            .first()
            .extracting(TestScenario::getFailureRate)
            .isEqualTo(BigDecimal.ZERO);
    }
}
