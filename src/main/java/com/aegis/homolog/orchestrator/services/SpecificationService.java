package com.aegis.homolog.orchestrator.services;

import com.aegis.homolog.orchestrator.exception.*;
import com.aegis.homolog.orchestrator.model.dto.CreateSpecificationRequestDTO;
import com.aegis.homolog.orchestrator.model.dto.SpecificationCreatedEvent;
import com.aegis.homolog.orchestrator.model.dto.SpecificationResponseDTO;
import com.aegis.homolog.orchestrator.model.entity.*;
import com.aegis.homolog.orchestrator.model.enums.HttpMethod;
import com.aegis.homolog.orchestrator.model.enums.SpecStatus;
import com.aegis.homolog.orchestrator.model.enums.SpecificationInputType;
import com.aegis.homolog.orchestrator.repository.*;
import com.aegis.homolog.orchestrator.services.messaging.SpecificationEventPublisherBase;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing Specifications.
 * Handles creation, validation, and event publishing for test specifications.
 * <p>
 * Supports two input modalities:
 * <ul>
 *   <li>MANUAL: User provides method, path, and request example directly</li>
 *   <li>API_CALL: User references an existing ApiCall from the catalog</li>
 * </ul>
 */
@Service
public class SpecificationService {

    private static final Logger log = LoggerFactory.getLogger(SpecificationService.class);

    private final SpecificationRepository specificationRepository;
    private final TestProjectRepository testProjectRepository;
    private final EnvironmentRepository environmentRepository;
    private final DomainRepository domainRepository;
    private final AuthProfileRepository authProfileRepository;
    private final ApiCallRepository apiCallRepository;
    private final SpecificationEventPublisherBase eventPublisher;

    public SpecificationService(
            SpecificationRepository specificationRepository,
            TestProjectRepository testProjectRepository,
            EnvironmentRepository environmentRepository,
            DomainRepository domainRepository,
            AuthProfileRepository authProfileRepository,
            ApiCallRepository apiCallRepository,
            SpecificationEventPublisherBase eventPublisher
    ) {
        this.specificationRepository = specificationRepository;
        this.testProjectRepository = testProjectRepository;
        this.environmentRepository = environmentRepository;
        this.domainRepository = domainRepository;
        this.authProfileRepository = authProfileRepository;
        this.apiCallRepository = apiCallRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new Specification for the given TestProject.
     *
     * @param testProjectId the TestProject ID
     * @param request       the creation request
     * @param userId        the user creating the specification
     * @return the created specification response
     * @throws TestProjectNotFoundException        if TestProject does not exist
     * @throws EnvironmentNotFoundException        if Environment does not exist or doesn't belong to TestProject
     * @throws ManualInputRequiredException        if inputType is MANUAL but method/path are missing
     * @throws ApiCallIdRequiredException          if inputType is API_CALL but apiCallId is missing
     * @throws ApiCallNotFoundException            if referenced ApiCall does not exist
     * @throws ApiCallInvalidException             if ApiCall doesn't belong to the TestProject
     * @throws DomainNotFoundException             if Domain is specified but does not exist
     * @throws AuthProfileRequiredException        if requiresAuth is true but authProfileId is null
     * @throws AuthProfileInvalidException         if AuthProfile doesn't belong to the Environment
     * @throws SupportingApiCallInvalidException   if any supporting ApiCall doesn't belong to the TestProject
     * @throws SpecificationAlreadyExistsException if a specification with same method+path+environment exists
     */
    @Transactional
    public SpecificationResponseDTO createSpecification(
            Long testProjectId,
            CreateSpecificationRequestDTO request,
            String userId
    ) {
        log.info("Creating specification '{}' for TestProject {} with inputType {}",
                request.name(), testProjectId, request.inputType());

        // 1. Validate TestProject exists
        TestProject testProject = testProjectRepository.findById(testProjectId)
                .orElseThrow(() -> new TestProjectNotFoundException(testProjectId));

        // 2. Validate Environment exists and belongs to TestProject
        Environment environment = environmentRepository.findByIdAndTestProjectId(
                        request.environmentId(), testProjectId)
                .orElseThrow(() -> new EnvironmentNotFoundException(request.environmentId(), testProjectId));

        // 3. Handle input type - resolve method, path, domain, and apiCall
        HttpMethod resolvedMethod;
        String resolvedPath;
        Domain resolvedDomain;
        ApiCall apiCall = null;

        if (request.inputType() == SpecificationInputType.MANUAL) {
            // Validate method and path are provided for MANUAL mode
            if (request.method() == null || request.path() == null || request.path().isBlank()) {
                throw new ManualInputRequiredException();
            }
            resolvedMethod = request.method();
            resolvedPath = request.path();
            resolvedDomain = resolveDomain(request.domainId());
        } else {
            // API_CALL mode - validate and fetch ApiCall
            if (request.apiCallId() == null) {
                throw new ApiCallIdRequiredException();
            }
            apiCall = validateAndFetchApiCall(request.apiCallId(), testProjectId);
            resolvedMethod = apiCall.getMethod();
            resolvedPath = apiCall.getRouteDefinition();
            // Use domain from request if provided, otherwise from ApiCall
            resolvedDomain = request.domainId() != null
                    ? resolveDomain(request.domainId())
                    : apiCall.getDomain();
        }

        // 4. Validate AuthProfile (if requiresAuth=true)
        AuthProfile authProfile = validateAuthProfile(request, environment);

        // 5. Validate supporting ApiCalls belong to project
        Set<ApiCall> supportingApiCalls = validateSupportingApiCalls(
                request.supportingApiCallIds(), testProjectId);

        // 6. Check for duplicate specification (method + path + environment)
        if (specificationRepository.existsByMethodAndPathAndEnvironmentId(
                resolvedMethod, resolvedPath, request.environmentId())) {
            throw new SpecificationAlreadyExistsException(
                    resolvedMethod, resolvedPath, request.environmentId());
        }

        // 7. Determine initial status
        SpecStatus initialStatus = Boolean.TRUE.equals(request.approveBeforeGeneration())
                ? SpecStatus.WAITING_APPROVAL
                : SpecStatus.CREATED;

        // 8. Build and persist Specification
        Specification specification = Specification.builder()
                .inputType(request.inputType())
                .testProject(testProject)
                .environment(environment)
                .domain(resolvedDomain)
                .authProfile(authProfile)
                .apiCall(apiCall)
                .name(request.name())
                .description(request.description())
                .testObjective(request.testObjective())
                .method(resolvedMethod)
                .path(resolvedPath)
                .requiresAuth(request.requiresAuth())
                .requestExample(toJsonString(request.requestExample()))
                .approveBeforeGeneration(request.approveBeforeGeneration())
                .status(initialStatus)
                .supportingApiCalls(supportingApiCalls)
                .createdBy(userId)
                .lastUpdatedBy(userId)
                .build();

        Specification savedSpecification = specificationRepository.save(specification);
        log.info("Specification created with ID: {} and status: {}",
                savedSpecification.getId(), savedSpecification.getStatus());

        // 9. Publish event if no approval required
        if (initialStatus == SpecStatus.CREATED) {
            SpecificationCreatedEvent event = SpecificationCreatedEvent.fromEntity(savedSpecification);
            eventPublisher.publishSpecificationCreated(event);
        }

        // 10. Return response
        return SpecificationResponseDTO.fromEntity(savedSpecification);
    }

    /**
     * Validates and fetches an ApiCall, ensuring it belongs to the specified project.
     */
    private ApiCall validateAndFetchApiCall(Long apiCallId, Long testProjectId) {
        return apiCallRepository.findByIdAndProjectId(apiCallId, testProjectId)
                .orElseThrow(() -> {
                    // Check if it exists at all to provide better error message
                    if (apiCallRepository.existsById(apiCallId)) {
                        return new ApiCallInvalidException(apiCallId, testProjectId);
                    }
                    return new ApiCallNotFoundException(apiCallId);
                });
    }

    /**
     * Resolves a Domain by ID, returning null if not provided.
     */
    private Domain resolveDomain(Long domainId) {
        if (domainId == null) {
            return null;
        }
        return domainRepository.findById(domainId)
                .orElseThrow(() -> new DomainNotFoundException(domainId));
    }

    /**
     * Validates AuthProfile if authentication is required.
     */
    private AuthProfile validateAuthProfile(CreateSpecificationRequestDTO request, Environment environment) {
        if (!Boolean.TRUE.equals(request.requiresAuth())) {
            return null;
        }

        if (request.authProfileId() == null) {
            throw new AuthProfileRequiredException();
        }

        // Validate AuthProfile belongs to the Environment
        if (!authProfileRepository.existsByIdAndEnvironmentId(
                request.authProfileId(), environment.getId())) {
            throw new AuthProfileInvalidException(request.authProfileId(), environment.getId());
        }

        return authProfileRepository.findById(request.authProfileId())
                .orElseThrow(() -> new AuthProfileNotFoundException(request.authProfileId()));
    }

    /**
     * Validates that all supporting ApiCall IDs belong to the project.
     */
    private Set<ApiCall> validateSupportingApiCalls(Set<Long> apiCallIds, Long testProjectId) {
        if (apiCallIds == null || apiCallIds.isEmpty()) {
            return new HashSet<>();
        }

        List<ApiCall> foundApiCalls = apiCallRepository.findAllByIdInAndProjectId(apiCallIds, testProjectId);

        if (foundApiCalls.size() != apiCallIds.size()) {
            Set<Long> foundIds = foundApiCalls.stream()
                    .map(ApiCall::getId)
                    .collect(Collectors.toSet());
            Set<Long> invalidIds = apiCallIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toSet());
            throw new SupportingApiCallInvalidException(testProjectId, invalidIds);
        }

        return new HashSet<>(foundApiCalls);
    }

    /**
     * Converts a JsonNode to its String representation.
     *
     * @param jsonNode the JSON node to convert
     * @return the JSON string, or null if input is null
     */
    private String toJsonString(JsonNode jsonNode) {
        if (jsonNode == null || jsonNode.isNull()) {
            return null;
        }
        return jsonNode.toString();
    }
}

