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
    private EnvironmentRepository environmentRepository;

    @Autowired
    private BaseUrlRepository baseUrlRepository;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ApiCallRepository apiCallRepository;

    @Autowired
    private TestScenarioRepository testScenarioRepository;

    @Test
    @DisplayName("Should store and query projects by projectId")
    void shouldStoreAndQueryProjectsByProjectId() {
        var project = TestEntityFactory.testProject(1L, "internal_systems");
        testProjectRepository.save(project);

        var results = testProjectRepository.findByProjectId(1L);

        assertThat(results)
                .hasSize(1)
                .first()
                .matches(saved -> saved.getProjectId().equals(1L)
                        && saved.getDescription().contains("Regression"));
    }

    @Test
    @DisplayName("Should fetch domains by name fragment")
    void shouldFetchDomainsByNameFragment() {
        var domain = TestEntityFactory.domain("Users");
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
        var regression = TestEntityFactory.tag("Regression", "scenario");
        var smoke = TestEntityFactory.tag("Smoke", "feature");
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
        // Setup project, environment, baseUrl, domain
        var project = testProjectRepository.save(TestEntityFactory.testProject(2L, "client_homologation"));
        var environment = environmentRepository.save(TestEntityFactory.environment(project, "DEV"));
        var baseUrl = baseUrlRepository.save(TestEntityFactory.baseUrl(project, environment, "MAIN_API"));
        var domain = domainRepository.save(TestEntityFactory.domain("Payments"));

        var apiCall = TestEntityFactory.apiCall(project, domain, baseUrl);
        apiCallRepository.save(apiCall);

        var results = apiCallRepository.findByProjectIdAndDomainId(project.getId(), domain.getId());

        assertThat(results)
                .hasSize(1)
                .first()
                .extracting(ApiCall::getRouteDefinition)
                .isEqualTo("/v1/payments");
    }

    @Test
    @DisplayName("Should locate scenarios by tag and project")
    void shouldLocateScenariosByTagAndProject() {
        var project = testProjectRepository.save(TestEntityFactory.testProject(3L, "internal_systems"));
        var tag = tagRepository.save(TestEntityFactory.tag("Regression", "scenario"));

        var scenario = TestEntityFactory.testScenario(project, List.of(tag.getId()));
        testScenarioRepository.save(scenario);

        var results = testScenarioRepository.findByProjectIdAndTagIdsContaining(project.getId(), tag.getId());

        assertThat(results)
            .hasSize(1)
            .first()
            .extracting(TestScenario::getFailureRate)
            .isEqualTo(BigDecimal.ZERO);
    }
}
