package com.aegis.homolog.orchestrator.service;

import com.aegis.homolog.orchestrator.exception.ApiCallIdRequiredException;
import com.aegis.homolog.orchestrator.exception.ApiCallInvalidException;
import com.aegis.homolog.orchestrator.exception.ApiCallNotFoundException;
import com.aegis.homolog.orchestrator.exception.AuthProfileInvalidException;
import com.aegis.homolog.orchestrator.exception.AuthProfileRequiredException;
import com.aegis.homolog.orchestrator.exception.DomainNotFoundException;
import com.aegis.homolog.orchestrator.exception.EnvironmentNotFoundException;
import com.aegis.homolog.orchestrator.exception.ManualInputRequiredException;
import com.aegis.homolog.orchestrator.exception.SpecificationAlreadyExistsException;
import com.aegis.homolog.orchestrator.exception.SupportingApiCallInvalidException;
import com.aegis.homolog.orchestrator.exception.TestProjectNotFoundException;
import com.aegis.homolog.orchestrator.model.dto.CreateSpecificationRequestDTO;
import com.aegis.homolog.orchestrator.model.dto.SpecificationCreatedEvent;
import com.aegis.homolog.orchestrator.model.dto.SpecificationResponseDTO;
import com.aegis.homolog.orchestrator.model.entity.ApiCall;
import com.aegis.homolog.orchestrator.model.entity.AuthProfile;
import com.aegis.homolog.orchestrator.model.entity.BaseUrl;
import com.aegis.homolog.orchestrator.model.entity.BearerTokenCredentials;
import com.aegis.homolog.orchestrator.model.entity.Domain;
import com.aegis.homolog.orchestrator.model.entity.Environment;
import com.aegis.homolog.orchestrator.model.entity.Specification;
import com.aegis.homolog.orchestrator.model.entity.TestProject;
import com.aegis.homolog.orchestrator.model.enums.HttpMethod;
import com.aegis.homolog.orchestrator.model.enums.SpecStatus;
import com.aegis.homolog.orchestrator.model.enums.SpecificationInputType;
import com.aegis.homolog.orchestrator.repository.*;
import com.aegis.homolog.orchestrator.services.SpecificationService;
import com.aegis.homolog.orchestrator.services.messaging.SpecificationEventPublisherBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpecificationService")
class SpecificationServiceTest {

    @Mock
    private SpecificationRepository specificationRepository;

    @Mock
    private TestProjectRepository testProjectRepository;

    @Mock
    private EnvironmentRepository environmentRepository;

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private AuthProfileRepository authProfileRepository;

    @Mock
    private ApiCallRepository apiCallRepository;

    @Mock
    private SpecificationEventPublisherBase eventPublisher;

    private SpecificationService specificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Captor
    private ArgumentCaptor<Specification> specificationCaptor;

    @Captor
    private ArgumentCaptor<SpecificationCreatedEvent> eventCaptor;

    private static final Long TEST_PROJECT_ID = 1L;
    private static final Long ENVIRONMENT_ID = 10L;
    private static final Long DOMAIN_ID = 100L;
    private static final Long AUTH_PROFILE_ID = 200L;
    private static final Long API_CALL_ID = 300L;
    private static final String USER_ID = "user-123";

    private TestProject testProject;
    private Environment environment;
    private Domain domain;
    private AuthProfile authProfile;
    private ApiCall apiCall;
    private BaseUrl baseUrl;

    @BeforeEach
    void setUp() {
        specificationService = new SpecificationService(
                specificationRepository,
                testProjectRepository,
                environmentRepository,
                domainRepository,
                authProfileRepository,
                apiCallRepository,
                eventPublisher
        );

        testProject = TestProject.builder()
                .id(TEST_PROJECT_ID)
                .projectId(999L)
                .name("Test Project")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy(USER_ID)
                .lastUpdatedBy(USER_ID)
                .build();

        environment = Environment.builder()
                .id(ENVIRONMENT_ID)
                .testProject(testProject)
                .name("DEV")
                .isDefault(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy(USER_ID)
                .lastUpdatedBy(USER_ID)
                .build();

        domain = Domain.builder()
                .id(DOMAIN_ID)
                .name("Invoices")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy(USER_ID)
                .lastUpdatedBy(USER_ID)
                .build();

        authProfile = AuthProfile.builder()
                .id(AUTH_PROFILE_ID)
                .environment(environment)
                .name("Bearer Token Profile")
                .credentials(BearerTokenCredentials.builder()
                        .id(1L)
                        .token("test-token")
                        .build())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy(USER_ID)
                .lastUpdatedBy(USER_ID)
                .build();

        baseUrl = BaseUrl.builder()
                .id(1L)
                .testProject(testProject)
                .environment(environment)
                .identifier("MAIN_API")
                .url("https://api.example.com")
                .build();

        apiCall = ApiCall.builder()
                .id(API_CALL_ID)
                .project(testProject)
                .domain(domain)
                .baseUrl(baseUrl)
                .routeDefinition("/api/v1/invoices")
                .method(HttpMethod.POST)
                .description("Creates a new invoice")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy(USER_ID)
                .lastUpdatedBy(USER_ID)
                .build();
    }

    @Nested
    @DisplayName("createSpecification - MANUAL mode")
    class ManualModeTests {

        private CreateSpecificationRequestDTO validManualRequest;

        @BeforeEach
        void setUp() {
            validManualRequest = new CreateSpecificationRequestDTO(
                    SpecificationInputType.MANUAL,
                    "Create Invoice",
                    "Creates a new invoice in the system",
                    "Validate all invoice creation scenarios including field validations and business rules",
                    DOMAIN_ID,
                    HttpMethod.POST,
                    "/api/v1/invoices",
                    null, // apiCallId not needed for MANUAL
                    true,
                    AUTH_PROFILE_ID,
                    ENVIRONMENT_ID,
                    createJsonNode("{\"customerId\": \"123\", \"amount\": 100.50}"),
                    null, // no supporting api calls
                    false
            );
        }

        @Test
        @DisplayName("should create Specification successfully with MANUAL input")
        void shouldCreateSpecificationWithManualInput() {
            // Arrange
            when(testProjectRepository.findById(TEST_PROJECT_ID)).thenReturn(Optional.of(testProject));
            when(environmentRepository.findByIdAndTestProjectId(ENVIRONMENT_ID, TEST_PROJECT_ID))
                    .thenReturn(Optional.of(environment));
            when(domainRepository.findById(DOMAIN_ID)).thenReturn(Optional.of(domain));
            when(authProfileRepository.existsByIdAndEnvironmentId(AUTH_PROFILE_ID, ENVIRONMENT_ID)).thenReturn(true);
            when(authProfileRepository.findById(AUTH_PROFILE_ID)).thenReturn(Optional.of(authProfile));
            when(specificationRepository.existsByMethodAndPathAndEnvironmentId(
                    HttpMethod.POST, "/api/v1/invoices", ENVIRONMENT_ID)).thenReturn(false);

            Specification savedSpec = createManualSpecification(500L, validManualRequest, SpecStatus.CREATED);
            when(specificationRepository.save(any(Specification.class))).thenReturn(savedSpec);

            // Act
            SpecificationResponseDTO response = specificationService.createSpecification(
                    TEST_PROJECT_ID, validManualRequest, USER_ID);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(500L);
            assertThat(response.inputType()).isEqualTo(SpecificationInputType.MANUAL);
            assertThat(response.name()).isEqualTo(validManualRequest.name());
            assertThat(response.testObjective()).isEqualTo(validManualRequest.testObjective());
            assertThat(response.method()).isEqualTo(HttpMethod.POST);
            assertThat(response.path()).isEqualTo("/api/v1/invoices");
            assertThat(response.status()).isEqualTo(SpecStatus.CREATED);
            assertThat(response.apiCallId()).isNull();

            verify(specificationRepository).save(specificationCaptor.capture());
            Specification captured = specificationCaptor.getValue();
            assertThat(captured.getInputType()).isEqualTo(SpecificationInputType.MANUAL);
            assertThat(captured.getApiCall()).isNull();
        }

        @Test
        @DisplayName("should throw ManualInputRequiredException when method is missing in MANUAL mode")
        void shouldThrowExceptionWhenMethodMissingInManualMode() {
            // Arrange
            var requestWithoutMethod = new CreateSpecificationRequestDTO(
                    SpecificationInputType.MANUAL,
                    "Create Invoice",
                    "Description",
                    "Test objective",
                    null,
                    null, // method is null
                    "/api/v1/invoices",
                    null,
                    false,
                    null,
                    ENVIRONMENT_ID,
                    null,
                    null,
                    false
            );

            when(testProjectRepository.findById(TEST_PROJECT_ID)).thenReturn(Optional.of(testProject));
            when(environmentRepository.findByIdAndTestProjectId(ENVIRONMENT_ID, TEST_PROJECT_ID))
                    .thenReturn(Optional.of(environment));

            // Act & Assert
            assertThatThrownBy(() -> specificationService.createSpecification(
                    TEST_PROJECT_ID, requestWithoutMethod, USER_ID))
                    .isInstanceOf(ManualInputRequiredException.class);

            verify(specificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ManualInputRequiredException when path is missing in MANUAL mode")
        void shouldThrowExceptionWhenPathMissingInManualMode() {
            // Arrange
            var requestWithoutPath = new CreateSpecificationRequestDTO(
                    SpecificationInputType.MANUAL,
                    "Create Invoice",
                    "Description",
                    "Test objective",
                    null,
                    HttpMethod.POST,
                    null, // path is null
                    null,
                    false,
                    null,
                    ENVIRONMENT_ID,
                    null,
                    null,
                    false
            );

            when(testProjectRepository.findById(TEST_PROJECT_ID)).thenReturn(Optional.of(testProject));
            when(environmentRepository.findByIdAndTestProjectId(ENVIRONMENT_ID, TEST_PROJECT_ID))
                    .thenReturn(Optional.of(environment));

            // Act & Assert
            assertThatThrownBy(() -> specificationService.createSpecification(
                    TEST_PROJECT_ID, requestWithoutPath, USER_ID))
                    .isInstanceOf(ManualInputRequiredException.class);

            verify(specificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should set status to WAITING_APPROVAL when approveBeforeGeneration is true")
        void shouldSetWaitingApprovalStatus() {
            // Arrange
            var requestWithApproval = new CreateSpecificationRequestDTO(
                    SpecificationInputType.MANUAL,
                    "Create Invoice",
                    null,
                    "Test objective",
                    null,
                    HttpMethod.POST,
                    "/api/v1/invoices",
                    null,
                    false,
                    null,
                    ENVIRONMENT_ID,
                    null,
                    null,
                    true  // approveBeforeGeneration = true
            );

            when(testProjectRepository.findById(TEST_PROJECT_ID)).thenReturn(Optional.of(testProject));
            when(environmentRepository.findByIdAndTestProjectId(ENVIRONMENT_ID, TEST_PROJECT_ID))
                    .thenReturn(Optional.of(environment));
            when(specificationRepository.existsByMethodAndPathAndEnvironmentId(
                    HttpMethod.POST, "/api/v1/invoices", ENVIRONMENT_ID)).thenReturn(false);

            Specification savedSpec = createManualSpecification(500L, requestWithApproval, SpecStatus.WAITING_APPROVAL);
            when(specificationRepository.save(any(Specification.class))).thenReturn(savedSpec);

            // Act
            SpecificationResponseDTO response = specificationService.createSpecification(
                    TEST_PROJECT_ID, requestWithApproval, USER_ID);

            // Assert
            assertThat(response.status()).isEqualTo(SpecStatus.WAITING_APPROVAL);
            verify(eventPublisher, never()).publishSpecificationCreated(any());
        }

        @Test
        @DisplayName("should publish event when approveBeforeGeneration is false")
        void shouldPublishEventWhenNoApprovalRequired() {
            // Arrange
            when(testProjectRepository.findById(TEST_PROJECT_ID)).thenReturn(Optional.of(testProject));
            when(environmentRepository.findByIdAndTestProjectId(ENVIRONMENT_ID, TEST_PROJECT_ID))
                    .thenReturn(Optional.of(environment));
            when(domainRepository.findById(DOMAIN_ID)).thenReturn(Optional.of(domain));
            when(authProfileRepository.existsByIdAndEnvironmentId(AUTH_PROFILE_ID, ENVIRONMENT_ID)).thenReturn(true);
            when(authProfileRepository.findById(AUTH_PROFILE_ID)).thenReturn(Optional.of(authProfile));
            when(specificationRepository.existsByMethodAndPathAndEnvironmentId(
                    HttpMethod.POST, "/api/v1/invoices", ENVIRONMENT_ID)).thenReturn(false);

            Specification savedSpec = createManualSpecification(500L, validManualRequest, SpecStatus.CREATED);
            when(specificationRepository.save(any(Specification.class))).thenReturn(savedSpec);

            // Act
            specificationService.createSpecification(TEST_PROJECT_ID, validManualRequest, USER_ID);

            // Assert
            verify(eventPublisher).publishSpecificationCreated(eventCaptor.capture());
            SpecificationCreatedEvent event = eventCaptor.getValue();
            assertThat(event.specificationId()).isEqualTo(500L);
            assertThat(event.inputType()).isEqualTo(SpecificationInputType.MANUAL);
            assertThat(event.testObjective()).isEqualTo(validManualRequest.testObjective());
        }
    }

    @Nested
    @DisplayName("createSpecification - API_CALL mode")
    class ApiCallModeTests {

        private CreateSpecificationRequestDTO validApiCallRequest;

        @BeforeEach
        void setUp() {
            validApiCallRequest = new CreateSpecificationRequestDTO(
                    SpecificationInputType.API_CALL,
                    "Invoice Creation Tests",
                    "Comprehensive tests for invoice creation",
                    "Validate all invoice creation scenarios including field validations and business rules",
                    null, // domain will be resolved from ApiCall
                    null, // method resolved from ApiCall
                    null, // path resolved from ApiCall
                    API_CALL_ID,
                    true,
                    AUTH_PROFILE_ID,
                    ENVIRONMENT_ID,
                    null,
                    null,
                    false
            );
        }

        @Test
        @DisplayName("should create Specification successfully with API_CALL input")
        void shouldCreateSpecificationWithApiCallInput() {
            // Arrange
            when(testProjectRepository.findById(TEST_PROJECT_ID)).thenReturn(Optional.of(testProject));
            when(environmentRepository.findByIdAndTestProjectId(ENVIRONMENT_ID, TEST_PROJECT_ID))
                    .thenReturn(Optional.of(environment));
            when(apiCallRepository.findByIdAndProjectId(API_CALL_ID, TEST_PROJECT_ID))
                    .thenReturn(Optional.of(apiCall));
            when(authProfileRepository.existsByIdAndEnvironmentId(AUTH_PROFILE_ID, ENVIRONMENT_ID)).thenReturn(true);
            when(authProfileRepository.findById(AUTH_PROFILE_ID)).thenReturn(Optional.of(authProfile));
            when(specificationRepository.existsByMethodAndPathAndEnvironmentId(
                    HttpMethod.POST, "/api/v1/invoices", ENVIRONMENT_ID)).thenReturn(false);

            Specification savedSpec = createApiCallSpecification(500L, validApiCallRequest, SpecStatus.CREATED);
            when(specificationRepository.save(any(Specification.class))).thenReturn(savedSpec);

            // Act
            SpecificationResponseDTO response = specificationService.createSpecification(
                    TEST_PROJECT_ID, validApiCallRequest, USER_ID);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(500L);
            assertThat(response.inputType()).isEqualTo(SpecificationInputType.API_CALL);
            assertThat(response.method()).isEqualTo(HttpMethod.POST); // from ApiCall
            assertThat(response.path()).isEqualTo("/api/v1/invoices"); // from ApiCall
            assertThat(response.apiCallId()).isEqualTo(API_CALL_ID);
            assertThat(response.domainId()).isEqualTo(DOMAIN_ID); // from ApiCall

            verify(specificationRepository).save(specificationCaptor.capture());
            Specification captured = specificationCaptor.getValue();
            assertThat(captured.getInputType()).isEqualTo(SpecificationInputType.API_CALL);
            assertThat(captured.getApiCall()).isEqualTo(apiCall);
        }

        @Test
        @DisplayName("should throw ApiCallIdRequiredException when apiCallId is missing in API_CALL mode")
        void shouldThrowExceptionWhenApiCallIdMissing() {
            // Arrange
            var requestWithoutApiCallId = new CreateSpecificationRequestDTO(
                    SpecificationInputType.API_CALL,
                    "Invoice Tests",
                    "Description",
                    "Test objective",
                    null,
                    null,
                    null,
                    null, // apiCallId is null
                    false,
                    null,
                    ENVIRONMENT_ID,
                    null,
                    null,
                    false
            );

            when(testProjectRepository.findById(TEST_PROJECT_ID)).thenReturn(Optional.of(testProject));
            when(environmentRepository.findByIdAndTestProjectId(ENVIRONMENT_ID, TEST_PROJECT_ID))
                    .thenReturn(Optional.of(environment));

            // Act & Assert
            assertThatThrownBy(() -> specificationService.createSpecification(
                    TEST_PROJECT_ID, requestWithoutApiCallId, USER_ID))
                    .isInstanceOf(ApiCallIdRequiredException.class);

            verify(specificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ApiCallNotFoundException when ApiCall does not exist")
        void shouldThrowExceptionWhenApiCallNotFound() {
            // Arrange
            when(testProjectRepository.findById(TEST_PROJECT_ID)).thenReturn(Optional.of(testProject));
            when(environmentRepository.findByIdAndTestProjectId(ENVIRONMENT_ID, TEST_PROJECT_ID))
                    .thenReturn(Optional.of(environment));
            when(apiCallRepository.findByIdAndProjectId(API_CALL_ID, TEST_PROJECT_ID))
                    .thenReturn(Optional.empty());
            when(apiCallRepository.existsById(API_CALL_ID)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> specificationService.createSpecification(
                    TEST_PROJECT_ID, validApiCallRequest, USER_ID))
                    .isInstanceOf(ApiCallNotFoundException.class)
                    .hasMessageContaining(String.valueOf(API_CALL_ID));

            verify(specificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ApiCallInvalidException when ApiCall belongs to different project")
        void shouldThrowExceptionWhenApiCallBelongsToDifferentProject() {
            // Arrange
            when(testProjectRepository.findById(TEST_PROJECT_ID)).thenReturn(Optional.of(testProject));
            when(environmentRepository.findByIdAndTestProjectId(ENVIRONMENT_ID, TEST_PROJECT_ID))
                    .thenReturn(Optional.of(environment));
            when(apiCallRepository.findByIdAndProjectId(API_CALL_ID, TEST_PROJECT_ID))
                    .thenReturn(Optional.empty());
            when(apiCallRepository.existsById(API_CALL_ID)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> specificationService.createSpecification(
                    TEST_PROJECT_ID, validApiCallRequest, USER_ID))
                    .isInstanceOf(ApiCallInvalidException.class)
                    .hasMessageContaining(String.valueOf(API_CALL_ID))
                    .hasMessageContaining(String.valueOf(TEST_PROJECT_ID));

            verify(specificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should use domain from request when provided, even in API_CALL mode")
        void shouldUseDomainFromRequestWhenProvided() {
            // Arrange
            Domain otherDomain = Domain.builder().id(999L).name("Other Domain").build();
            var requestWithDomain = new CreateSpecificationRequestDTO(
                    SpecificationInputType.API_CALL,
                    "Invoice Tests",
                    "Description",
                    "Test objective",
                    999L, // explicit domain
                    null,
                    null,
                    API_CALL_ID,
                    false,
                    null,
                    ENVIRONMENT_ID,
                    null,
                    null,
                    false
            );

            when(testProjectRepository.findById(TEST_PROJECT_ID)).thenReturn(Optional.of(testProject));
            when(environmentRepository.findByIdAndTestProjectId(ENVIRONMENT_ID, TEST_PROJECT_ID))
                    .thenReturn(Optional.of(environment));
            when(apiCallRepository.findByIdAndProjectId(API_CALL_ID, TEST_PROJECT_ID))
                    .thenReturn(Optional.of(apiCall));
            when(domainRepository.findById(999L)).thenReturn(Optional.of(otherDomain));
            when(specificationRepository.existsByMethodAndPathAndEnvironmentId(any(), any(), any())).thenReturn(false);

            Specification savedSpec = Specification.builder()
                    .id(500L)
                    .inputType(SpecificationInputType.API_CALL)
                    .testProject(testProject)
                    .environment(environment)
                    .domain(otherDomain)
                    .apiCall(apiCall)
                    .name("Invoice Tests")
                    .testObjective("Test objective")
                    .method(HttpMethod.POST)
                    .path("/api/v1/invoices")
                    .requiresAuth(false)
                    .approveBeforeGeneration(false)
                    .status(SpecStatus.CREATED)
                    .supportingApiCalls(new HashSet<>())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .createdBy(USER_ID)
                    .lastUpdatedBy(USER_ID)
                    .build();
            when(specificationRepository.save(any(Specification.class))).thenReturn(savedSpec);

            // Act
            SpecificationResponseDTO response = specificationService.createSpecification(
                    TEST_PROJECT_ID, requestWithDomain, USER_ID);

            // Assert
            assertThat(response.domainId()).isEqualTo(999L);
        }
    }

    @Nested
    @DisplayName("createSpecification - supporting ApiCalls validation")
    class SupportingApiCallsTests {

        @Test
        @DisplayName("should add supporting ApiCalls when valid")
        void shouldAddSupportingApiCallsWhenValid() {
            // Arrange
            Set<Long> supportingIds = Set.of(301L, 302L);
            ApiCall support1 = ApiCall.builder().id(301L).project(testProject).build();
            ApiCall support2 = ApiCall.builder().id(302L).project(testProject).build();

            var request = new CreateSpecificationRequestDTO(
                    SpecificationInputType.MANUAL,
                    "Invoice Tests",
                    "Description",
                    "Test objective",
                    null,
                    HttpMethod.POST,
                    "/api/v1/invoices",
                    null,
                    false,
                    null,
                    ENVIRONMENT_ID,
                    null,
                    supportingIds,
                    false
            );

            when(testProjectRepository.findById(TEST_PROJECT_ID)).thenReturn(Optional.of(testProject));
            when(environmentRepository.findByIdAndTestProjectId(ENVIRONMENT_ID, TEST_PROJECT_ID))
                    .thenReturn(Optional.of(environment));
            when(apiCallRepository.findAllByIdInAndProjectId(supportingIds, TEST_PROJECT_ID))
                    .thenReturn(List.of(support1, support2));
            when(specificationRepository.existsByMethodAndPathAndEnvironmentId(any(), any(), any())).thenReturn(false);

            Specification savedSpec = Specification.builder()
                    .id(500L)
                    .inputType(SpecificationInputType.MANUAL)
                    .testProject(testProject)
                    .environment(environment)
                    .name("Invoice Tests")
                    .testObjective("Test objective")
                    .method(HttpMethod.POST)
                    .path("/api/v1/invoices")
                    .requiresAuth(false)
                    .approveBeforeGeneration(false)
                    .status(SpecStatus.CREATED)
                    .supportingApiCalls(Set.of(support1, support2))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .createdBy(USER_ID)
                    .lastUpdatedBy(USER_ID)
                    .build();
            when(specificationRepository.save(any(Specification.class))).thenReturn(savedSpec);

            // Act
            SpecificationResponseDTO response = specificationService.createSpecification(
                    TEST_PROJECT_ID, request, USER_ID);

            // Assert
            assertThat(response.supportingApiCallIds()).containsExactlyInAnyOrder(301L, 302L);
        }

        @Test
        @DisplayName("should throw SupportingApiCallInvalidException when some IDs are invalid")
        void shouldThrowExceptionWhenSupportingApiCallsInvalid() {
            // Arrange
            Set<Long> supportingIds = Set.of(301L, 302L, 999L); // 999L doesn't belong to project
            ApiCall support1 = ApiCall.builder().id(301L).project(testProject).build();
            ApiCall support2 = ApiCall.builder().id(302L).project(testProject).build();

            var request = new CreateSpecificationRequestDTO(
                    SpecificationInputType.MANUAL,
                    "Invoice Tests",
                    "Description",
                    "Test objective",
                    null,
                    HttpMethod.POST,
                    "/api/v1/invoices",
                    null,
                    false,
                    null,
                    ENVIRONMENT_ID,
                    null,
                    supportingIds,
                    false
            );

            when(testProjectRepository.findById(TEST_PROJECT_ID)).thenReturn(Optional.of(testProject));
            when(environmentRepository.findByIdAndTestProjectId(ENVIRONMENT_ID, TEST_PROJECT_ID))
                    .thenReturn(Optional.of(environment));
            when(apiCallRepository.findAllByIdInAndProjectId(supportingIds, TEST_PROJECT_ID))
                    .thenReturn(List.of(support1, support2)); // only 2 found, not 3

            // Act & Assert
            assertThatThrownBy(() -> specificationService.createSpecification(
                    TEST_PROJECT_ID, request, USER_ID))
                    .isInstanceOf(SupportingApiCallInvalidException.class)
                    .hasMessageContaining("999");

            verify(specificationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("createSpecification - common validations")
    class CommonValidationTests {

        @Test
        @DisplayName("should throw TestProjectNotFoundException when TestProject does not exist")
        void shouldThrowExceptionWhenTestProjectNotFound() {
            // Arrange
            var request = createMinimalManualRequest();
            when(testProjectRepository.findById(TEST_PROJECT_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> specificationService.createSpecification(
                    TEST_PROJECT_ID, request, USER_ID))
                    .isInstanceOf(TestProjectNotFoundException.class);

            verify(specificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw EnvironmentNotFoundException when Environment not in TestProject")
        void shouldThrowExceptionWhenEnvironmentNotFound() {
            // Arrange
            var request = createMinimalManualRequest();
            when(testProjectRepository.findById(TEST_PROJECT_ID)).thenReturn(Optional.of(testProject));
            when(environmentRepository.findByIdAndTestProjectId(ENVIRONMENT_ID, TEST_PROJECT_ID))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> specificationService.createSpecification(
                    TEST_PROJECT_ID, request, USER_ID))
                    .isInstanceOf(EnvironmentNotFoundException.class);

            verify(specificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw DomainNotFoundException when Domain does not exist")
        void shouldThrowExceptionWhenDomainNotFound() {
            // Arrange
            var request = new CreateSpecificationRequestDTO(
                    SpecificationInputType.MANUAL,
                    "Test",
                    null,
                    "Objective",
                    DOMAIN_ID,
                    HttpMethod.GET,
                    "/api/test",
                    null,
                    false,
                    null,
                    ENVIRONMENT_ID,
                    null,
                    null,
                    false
            );

            when(testProjectRepository.findById(TEST_PROJECT_ID)).thenReturn(Optional.of(testProject));
            when(environmentRepository.findByIdAndTestProjectId(ENVIRONMENT_ID, TEST_PROJECT_ID))
                    .thenReturn(Optional.of(environment));
            when(domainRepository.findById(DOMAIN_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> specificationService.createSpecification(
                    TEST_PROJECT_ID, request, USER_ID))
                    .isInstanceOf(DomainNotFoundException.class);

            verify(specificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw AuthProfileRequiredException when requiresAuth but no authProfileId")
        void shouldThrowExceptionWhenAuthProfileRequired() {
            // Arrange
            var request = new CreateSpecificationRequestDTO(
                    SpecificationInputType.MANUAL,
                    "Test",
                    null,
                    "Objective",
                    null,
                    HttpMethod.GET,
                    "/api/test",
                    null,
                    true,  // requiresAuth
                    null,  // no authProfileId
                    ENVIRONMENT_ID,
                    null,
                    null,
                    false
            );

            when(testProjectRepository.findById(TEST_PROJECT_ID)).thenReturn(Optional.of(testProject));
            when(environmentRepository.findByIdAndTestProjectId(ENVIRONMENT_ID, TEST_PROJECT_ID))
                    .thenReturn(Optional.of(environment));

            // Act & Assert
            assertThatThrownBy(() -> specificationService.createSpecification(
                    TEST_PROJECT_ID, request, USER_ID))
                    .isInstanceOf(AuthProfileRequiredException.class);

            verify(specificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw AuthProfileInvalidException when AuthProfile not in Environment")
        void shouldThrowExceptionWhenAuthProfileInvalid() {
            // Arrange
            var request = new CreateSpecificationRequestDTO(
                    SpecificationInputType.MANUAL,
                    "Test",
                    null,
                    "Objective",
                    null,
                    HttpMethod.GET,
                    "/api/test",
                    null,
                    true,
                    AUTH_PROFILE_ID,
                    ENVIRONMENT_ID,
                    null,
                    null,
                    false
            );

            when(testProjectRepository.findById(TEST_PROJECT_ID)).thenReturn(Optional.of(testProject));
            when(environmentRepository.findByIdAndTestProjectId(ENVIRONMENT_ID, TEST_PROJECT_ID))
                    .thenReturn(Optional.of(environment));
            when(authProfileRepository.existsByIdAndEnvironmentId(AUTH_PROFILE_ID, ENVIRONMENT_ID))
                    .thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> specificationService.createSpecification(
                    TEST_PROJECT_ID, request, USER_ID))
                    .isInstanceOf(AuthProfileInvalidException.class);

            verify(specificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw SpecificationAlreadyExistsException for duplicate method+path+environment")
        void shouldThrowExceptionForDuplicateSpecification() {
            // Arrange
            var request = createMinimalManualRequest();
            when(testProjectRepository.findById(TEST_PROJECT_ID)).thenReturn(Optional.of(testProject));
            when(environmentRepository.findByIdAndTestProjectId(ENVIRONMENT_ID, TEST_PROJECT_ID))
                    .thenReturn(Optional.of(environment));
            when(specificationRepository.existsByMethodAndPathAndEnvironmentId(
                    HttpMethod.GET, "/api/test", ENVIRONMENT_ID)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> specificationService.createSpecification(
                    TEST_PROJECT_ID, request, USER_ID))
                    .isInstanceOf(SpecificationAlreadyExistsException.class);

            verify(specificationRepository, never()).save(any());
        }
    }

    // Helper methods

    private CreateSpecificationRequestDTO createMinimalManualRequest() {
        return new CreateSpecificationRequestDTO(
                SpecificationInputType.MANUAL,
                "Test Spec",
                null,
                "Test objective",
                null,
                HttpMethod.GET,
                "/api/test",
                null,
                false,
                null,
                ENVIRONMENT_ID,
                null,
                null,
                false
        );
    }

    private Specification createManualSpecification(Long id, CreateSpecificationRequestDTO request, SpecStatus status) {
        Instant now = Instant.now();
        return Specification.builder()
                .id(id)
                .inputType(SpecificationInputType.MANUAL)
                .testProject(testProject)
                .environment(environment)
                .domain(request.domainId() != null ? domain : null)
                .authProfile(request.authProfileId() != null ? authProfile : null)
                .apiCall(null)
                .name(request.name())
                .description(request.description())
                .testObjective(request.testObjective())
                .method(request.method())
                .path(request.path())
                .requiresAuth(request.requiresAuth())
                .requestExample(jsonNodeToString(request.requestExample()))
                .approveBeforeGeneration(request.approveBeforeGeneration())
                .status(status)
                .supportingApiCalls(new HashSet<>())
                .createdAt(now)
                .updatedAt(now)
                .createdBy(USER_ID)
                .lastUpdatedBy(USER_ID)
                .build();
    }

    private Specification createApiCallSpecification(Long id, CreateSpecificationRequestDTO request, SpecStatus status) {
        Instant now = Instant.now();
        return Specification.builder()
                .id(id)
                .inputType(SpecificationInputType.API_CALL)
                .testProject(testProject)
                .environment(environment)
                .domain(apiCall.getDomain())
                .authProfile(request.authProfileId() != null ? authProfile : null)
                .apiCall(apiCall)
                .name(request.name())
                .description(request.description())
                .testObjective(request.testObjective())
                .method(apiCall.getMethod())
                .path(apiCall.getRouteDefinition())
                .requiresAuth(request.requiresAuth())
                .requestExample(jsonNodeToString(request.requestExample()))
                .approveBeforeGeneration(request.approveBeforeGeneration())
                .status(status)
                .supportingApiCalls(new HashSet<>())
                .createdAt(now)
                .updatedAt(now)
                .createdBy(USER_ID)
                .lastUpdatedBy(USER_ID)
                .build();
    }

    private JsonNode createJsonNode(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON: " + json, e);
        }
    }

    private String jsonNodeToString(JsonNode node) {
        return node != null ? node.toString() : null;
    }
}

