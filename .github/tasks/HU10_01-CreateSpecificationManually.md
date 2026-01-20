# Task: HU10_01 - Create Specification Manually

> **User Story:** As an Administrator or Collaborator, I want to create a manual API test specification to provide structured context for AI-driven automated test generation.

---

## Overview

This task implements the `POST /v1/test-projects/{testProjectId}/specifications` endpoint that allows users to create a **Specification** - the functional source of truth used by the AI to plan scenarios, define steps, and generate automated tests (KarateDSL).

### Key Features
- **Two input modalities:** MANUAL (direct input) or API_CALL (reference existing endpoint)
- **ApiCall catalog:** Reusable endpoint definitions with BaseUrl relationship
- **Polymorphic authentication:** BearerToken or BasicAuth with encryption
- **Async communication:** RabbitMQ for aegis-agents (Python) integration
- **Supporting endpoints:** Additional ApiCalls for validation context

---

## Prerequisites (Already Implemented ✅)

| Entity | Status | Notes |
|--------|--------|-------|
| `TestProject` | ✅ | Root container |
| `Environment` | ✅ | Execution context |
| `Domain` | ✅ | Semantic grouping |
| `AuthProfile` | ✅ | Auth profile with credentials |
| `AuthCredentials` | ✅ | Polymorphic (Bearer/Basic) |
| `BearerTokenCredentials` | ✅ | Token-based auth |
| `BasicAuthCredentials` | ✅ | Username/password auth |
| `Specification` | ✅ | Test specification entity |
| `EncryptionService` | ✅ | AES encryption for credentials |
| `EncryptedStringConverter` | ✅ | JPA converter for auto encrypt/decrypt |

---

## Implementation Plan

### Phase 1: New/Updated Entities

#### 1.1 Create `BaseUrl` Entity (NEW)

**Table:** `t_aegis_base_urls`

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `id` | BIGINT | ❌ | PK, auto-increment |
| `test_project_id` | BIGINT | ❌ | FK to TestProject |
| `environment_id` | BIGINT | ❌ | FK to Environment |
| `identifier` | VARCHAR(100) | ❌ | Unique name (e.g., "INVOICES_API") |
| `url` | VARCHAR(500) | ❌ | Full base URL |
| `created_at` | TIMESTAMP | ❌ | Audit field |
| `updated_at` | TIMESTAMP | ❌ | Audit field |
| `created_by` | VARCHAR(64) | ❌ | Audit field |
| `last_updated_by` | VARCHAR(64) | ❌ | Audit field |

**Indexes:**
- `uk_base_url_project_env_identifier` → `(test_project_id, environment_id, identifier)` UNIQUE

**Relationships:**
- N:1 → `TestProject`
- N:1 → `Environment`

#### 1.2 Update `ApiCall` Entity

**Table:** `t_aegis_api_calls` (updated)

| Field | Type | Nullable | Description | Change |
|-------|------|----------|-------------|--------|
| `id` | BIGINT | ❌ | PK | - |
| `project_id` | BIGINT | ❌ | FK to TestProject | - |
| `domain_id` | BIGINT | ✅ | FK to Domain | **Now optional** |
| `base_url_id` | BIGINT | ❌ | FK to BaseUrl | **Changed to FK** |
| `route_definition` | VARCHAR(256) | ❌ | Path definition | - |
| `method` | VARCHAR(16) | ❌ | HttpMethod enum | **Use enum** |
| `description` | VARCHAR(500) | ✅ | What this endpoint does | **NEW** |
| `request_example` | TEXT | ✅ | JSON request example | **NEW** |
| `response_example` | TEXT | ✅ | JSON response example | **NEW** |
| `custom_variables` | Collection | ✅ | Variable IDs used | - |
| `base_gherkin` | TEXT | ❌ | Base KarateDSL | - |
| `required_params` | TEXT | ✅ | Required parameters | - |

**Relationships:**
- N:1 → `TestProject`
- N:1 → `Domain` (optional)
- N:1 → `BaseUrl` (new)

#### 1.3 Create `SpecificationInputType` Enum (NEW)

```java
public enum SpecificationInputType {
    MANUAL,
    API_CALL
}
```

#### 1.4 Update `Specification` Entity

| Field | Type | Nullable | Description | Change |
|-------|------|----------|-------------|--------|
| `input_type` | VARCHAR(20) | ❌ | MANUAL or API_CALL | **NEW** |
| `api_call_id` | BIGINT | ✅ | FK to ApiCall (when API_CALL) | **NEW** |
| `test_objective` | VARCHAR(2000) | ❌ | Detailed test objective | **NEW** |
| **Remove:** `request_example_type` | - | - | No longer needed | **REMOVED** |

**New relationship:**
- N:1 → `ApiCall` (optional, when inputType=API_CALL)

**New join table:** `t_aegis_specification_supporting_api_calls`

| Field | Type | Description |
|-------|------|-------------|
| `specification_id` | BIGINT | FK to Specification |
| `api_call_id` | BIGINT | FK to ApiCall |

---

### Phase 2: DTOs

#### 2.1 Request DTO (Updated)

**`CreateSpecificationRequestDTO`**

```java
public record CreateSpecificationRequestDTO(
    // Discriminator
    @NotNull SpecificationInputType inputType,
    
    // Common fields
    @NotBlank @Size(max = 255) String name,
    @Size(max = 1000) String description,
    @NotBlank @Size(max = 2000) String testObjective,
    Long domainId,
    @NotNull Boolean requiresAuth,
    Long authProfileId,
    @NotNull Long environmentId,
    Set<Long> supportingApiCallIds,
    @NotNull Boolean approveBeforeGeneration,
    
    // MANUAL mode fields
    HttpMethod method,                    // Required if inputType=MANUAL
    @Size(max = 500) String path,         // Required if inputType=MANUAL
    String requestExample,
    
    // API_CALL mode fields
    Long apiCallId                        // Required if inputType=API_CALL
) {}
```

#### 2.2 Response DTO (Updated)

**`SpecificationResponseDTO`**

```java
public record SpecificationResponseDTO(
    Long id,
    SpecificationInputType inputType,
    String name,
    String description,
    String testObjective,
    HttpMethod method,        // Resolved from ApiCall if API_CALL mode
    String path,              // Resolved from ApiCall if API_CALL mode
    SpecStatus status,
    Long domainId,
    Long environmentId,
    Long apiCallId,           // null if MANUAL
    Set<Long> supportingApiCallIds,
    Instant createdAt,
    Instant updatedAt
) {}
```

---

### Phase 3: New Repositories

#### 3.1 `BaseUrlRepository`

```java
public interface BaseUrlRepository extends JpaRepository<BaseUrl, Long> {
    boolean existsByTestProjectIdAndEnvironmentIdAndIdentifier(
        Long testProjectId, Long environmentId, String identifier);
    
    Optional<BaseUrl> findByTestProjectIdAndEnvironmentIdAndIdentifier(
        Long testProjectId, Long environmentId, String identifier);
}
```

#### 3.2 Update `ApiCallRepository`

```java
public interface ApiCallRepository extends JpaRepository<ApiCall, Long> {
    List<ApiCall> findByProjectIdAndDomainId(Long projectId, Long domainId);
    List<ApiCall> findByProjectId(Long projectId);
    boolean existsByIdAndProjectId(Long id, Long projectId);
    List<ApiCall> findAllByIdInAndProjectId(Set<Long> ids, Long projectId);
}
```

---

### Phase 4: New Exceptions

| Exception Class | Error Code | HTTP Status | When |
|-----------------|------------|-------------|------|
| `ApiCallNotFoundException` | `API_CALL_NOT_FOUND` | 404 | ApiCall not found |
| `ApiCallInvalidException` | `API_CALL_INVALID` | 422 | ApiCall doesn't belong to TestProject |
| `SupportingApiCallInvalidException` | `SUPPORTING_API_CALL_INVALID` | 422 | Supporting ApiCall doesn't belong to TestProject |
| `BaseUrlNotFoundException` | `BASE_URL_NOT_FOUND` | 404 | BaseUrl not found |

---

### Phase 5: Service Layer Updates

#### 5.1 Update `SpecificationService`

**New validation logic:**

```
public SpecificationResponseDTO createSpecification(
    Long testProjectId,
    CreateSpecificationRequestDTO request,
    String userId
) {
    // 1. Validate TestProject exists
    // 2. Validate Environment belongs to TestProject
    
    // 3. Handle input type
    HttpMethod resolvedMethod;
    String resolvedPath;
    Domain resolvedDomain;
    ApiCall apiCall;
    
    if (request.inputType() == MANUAL) {
        // Validate method and path are provided
        resolvedMethod = request.method();
        resolvedPath = request.path();
        resolvedDomain = validateDomain(request.domainId());
    } else { // API_CALL
        // Validate apiCallId is provided and belongs to project
        apiCall = validateApiCall(request.apiCallId(), testProjectId);
        resolvedMethod = apiCall.getMethod();
        resolvedPath = apiCall.getRouteDefinition();
        resolvedDomain = apiCall.getDomain();
    }
    
    // 4. Validate AuthProfile if requiresAuth
    // 5. Validate supportingApiCallIds belong to project
    // 6. Check for duplicates (method + path + environment)
    // 7. Determine status
    // 8. Persist
    // 9. Publish event if approveBeforeGeneration=false
    // 10. Return DTO
}
```

---

### Phase 6: RabbitMQ Integration (Already Configured ✅)

The RabbitMQ setup is already in place. We need to:

#### 6.1 Update Event DTO

**`SpecificationCreatedEvent`** (updated)

```java
public record SpecificationCreatedEvent(
    Long specificationId,
    Long testProjectId,
    Long environmentId,
    SpecificationInputType inputType,
    String method,
    String path,
    String testObjective,
    String requestExample,
    Set<Long> supportingApiCallIds,
    Instant createdAt
) {}
```

---

### Phase 7: Controller Updates

#### 7.1 Update `SpecificationController`

Swagger documentation updates for:
- New `inputType` discriminator field
- Conditional field validation descriptions
- `supportingApiCallIds` field
- `testObjective` field
- Updated response schema

---

### Phase 8: Validation Rules Implementation

#### Field Validations (RC10.01.x)

| Code | Field | Validation | Error Code |
|------|-------|------------|------------|
| RC10.01.1 | `name` | @NotBlank, @Size(max=255) | `REQUIRED_FIELD` |
| RC10.01.2 | `inputType` | @NotNull, valid enum | `REQUIRED_FIELD` |
| RC10.01.3 | `method` | Required if inputType=MANUAL | `REQUIRED_FIELD` |
| RC10.01.4 | `path` | Required if inputType=MANUAL, starts with `/` | `INVALID_API_PATH` |
| RC10.01.5 | `apiCallId` | Required if inputType=API_CALL | `REQUIRED_FIELD` |
| RC10.01.6 | `authProfileId` | Required if requiresAuth=true | `AUTH_PROFILE_REQUIRED` |
| RC10.01.7 | `testObjective` | @NotBlank, @Size(max=2000) | `REQUIRED_FIELD` |

#### Business Validations (RN10.01.x)

| Code | Rule | Error Code | HTTP |
|------|------|------------|------|
| RN10.01.1 | User permission | **TODO** | 403 |
| RN10.01.2 | ApiCall exists (if API_CALL) | `API_CALL_NOT_FOUND` | 404 |
| RN10.01.3 | ApiCall belongs to TestProject | `API_CALL_INVALID` | 422 |
| RN10.01.4 | Domain exists (if provided) | `DOMAIN_NOT_FOUND` | 422 |
| RN10.01.5 | AuthProfile in Environment | `AUTH_PROFILE_INVALID` | 422 |
| RN10.01.6 | No duplicate spec | `SPEC_ALREADY_EXISTS` | 409 |
| RN10.01.7 | SupportingApiCalls in Project | `SUPPORTING_API_CALL_INVALID` | 422 |

---

## File Structure (New/Modified Files)

```
src/main/java/com/aegis/homolog/orchestrator/
├── model/
│   ├── dto/
│   │   ├── CreateSpecificationRequestDTO.java     [MODIFY]
│   │   ├── SpecificationResponseDTO.java          [MODIFY]
│   │   └── SpecificationCreatedEvent.java         [MODIFY]
│   ├── entity/
│   │   ├── BaseUrl.java                           [NEW]
│   │   ├── ApiCall.java                           [MODIFY]
│   │   └── Specification.java                     [MODIFY]
│   └── enums/
│       └── SpecificationInputType.java            [NEW]
├── repository/
│   ├── BaseUrlRepository.java                     [NEW]
│   └── ApiCallRepository.java                     [MODIFY]
├── exception/
│   ├── ApiCallNotFoundException.java              [NEW]
│   ├── ApiCallInvalidException.java               [NEW]
│   ├── SupportingApiCallInvalidException.java     [NEW]
│   └── BaseUrlNotFoundException.java              [NEW]
├── services/
│   └── SpecificationService.java                  [MODIFY]
└── controller/
    └── SpecificationController.java               [MODIFY]
```

---

## Testing Strategy

### Unit Tests

1. **SpecificationServiceTest** (update)
   - Test MANUAL input mode validation
   - Test API_CALL input mode validation
   - Test conditional field validation
   - Test method/path resolution from ApiCall
   - Test supportingApiCallIds validation
   - Test status determination logic
   - Test event publishing decision

2. **SpecificationControllerTest** (update)
   - Test endpoint with MANUAL mode
   - Test endpoint with API_CALL mode
   - Test validation errors for each mode
   - Test response structure

3. **BaseUrlRepositoryTest** (new)
   - Test unique constraint
   - Test finder methods

4. **ApiCallRepositoryTest** (update)
   - Test new finder methods
   - Test project ownership validation

---

## Execution Order

| Step | Description | TDD |
|------|-------------|-----|
| 1 | Create `SpecificationInputType` enum | ❌ |
| 2 | Create `BaseUrl` entity | ❌ |
| 3 | Update `ApiCall` entity (add description, examples, enum method) | ❌ |
| 4 | Update `Specification` entity (add inputType, apiCall, testObjective) | ❌ |
| 5 | Create `BaseUrlRepository` | ❌ |
| 6 | Update `ApiCallRepository` | ❌ |
| 7 | Create new exception classes | ❌ |
| 8 | Update DTOs | ❌ |
| 9 | **Write SpecificationService tests** | ✅ |
| 10 | Update SpecificationService | ✅ |
| 11 | **Write SpecificationController tests** | ✅ |
| 12 | Update SpecificationController | ✅ |
| 13 | Update Swagger documentation | ❌ |
| 14 | Run all tests | ✅ |
| 15 | Manual testing via Swagger | ✅ |

---

## TODOs (Deferred)

| Item | Description |
|------|-------------|
| 👤 User Permissions | Validate RN10.01.1 (user is ADMIN/OWNER) |
| 📦 BaseUrl CRUD | Full CRUD endpoints for BaseUrl management |
| 📦 ApiCall CRUD | Full CRUD endpoints for ApiCall catalog management |
| 🔄 Domain Suggestion | Auto-suggest domain based on ApiCall's domain |
| 🔄 Supporting ApiCall Suggestion | Auto-suggest endpoints from same domain |

---

## Acceptance Criteria

- [ ] Create specification with MANUAL input mode
- [ ] Create specification with API_CALL input mode
- [ ] Resolve method/path from ApiCall when API_CALL mode
- [ ] Validate conditional fields based on inputType
- [ ] Validate supportingApiCallIds belong to project
- [ ] Validate testObjective is required
- [ ] All field validations return 400 with proper error codes
- [ ] Business rule violations return 404/422/409 with proper error codes
- [ ] Status is correctly determined based on `approveBeforeGeneration`
- [ ] Event is published to RabbitMQ with updated payload
- [ ] Response includes all specification data
- [ ] Swagger documentation is complete
- [ ] All unit tests pass

---

## Notes

- **RabbitMQ** chosen for MVP due to simpler local setup compared to Kafka
- **Event consumption** will be implemented in `aegis-agents` (Python service)
- **ApiCall catalog** serves as a reusable registry of endpoints
- **BaseUrl** allows environment-specific URL configuration
- **supportingApiCallIds** provides context for the AI to understand related endpoints for validation scenarios

