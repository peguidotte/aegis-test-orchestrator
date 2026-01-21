# Coding Standards - Aegis Homolog Orchestrator

This document describes the coding standards and patterns adopted in this project.

---

## 1. Language Standard

- **All code, comments, documentation, and commit messages MUST be written in English.**
- Even if user instructions are provided in Portuguese, always generate outputs in English.

---

## 2. Dependency Injection

**Always use Constructor Injection** instead of `@Autowired` on fields.

```java
// ❌ Avoid
@Autowired
private TestProjectService testProjectService;

// ✅ Preferred
private final TestProjectService testProjectService;

public TestProjectController(TestProjectService testProjectService) {
    this.testProjectService = testProjectService;
}
```

### Benefits

| Aspect | Field Injection | Constructor Injection |
|--------|-----------------|----------------------|
| **Testability** | ❌ Hard to mock | ✅ Easy to inject mocks |
| **Immutability** | ❌ Mutable field | ✅ Can be `final` |
| **Explicit dependencies** | ❌ Hidden | ✅ Visible in constructor |
| **Lifecycle** | ❌ Can be null before injection | ✅ Guaranteed at construction |

---

## 3. Exception Handling

### 3.1 Exception Hierarchy

All business exceptions extend `BusinessException`:

```
BusinessException (abstract)
  ├── TestProjectLimitReachedException (422)
  ├── TestProjectNameAlreadyExistsException (409)
  └── ... future exceptions
```

### 3.2 Error Code Naming Convention

Use **suffix-based naming** for error codes:

```
{ENTITY}_{ERROR_TYPE}
```

| Suffix | Use Case | HTTP Status |
|--------|----------|-------------|
| `*_LIMIT_REACHED` | Quantity limits | 422 |
| `*_ALREADY_EXISTS` | Duplicates | 409 |
| `*_NOT_FOUND` | Resource not found | 404 |
| `*_INVALID` | Invalid value | 400 |

### 3.3 Examples

| Error Code | Description |
|------------|-------------|
| `TEST_PROJECT_LIMIT_REACHED` | TestProject limit per project reached |
| `TEST_PROJECT_NAME_ALREADY_EXISTS` | TestProject name already exists |
| `ENVIRONMENT_LIMIT_REACHED` | Environment limit reached |
| `ENVIRONMENT_NAME_ALREADY_EXISTS` | Environment name already exists |
| `SCENARIO_NOT_FOUND` | Scenario not found |
| `API_CALL_INVALID` | Invalid API call |

### 3.4 Benefits of Suffix Pattern

- **Filter by entity**: `TEST_PROJECT_*`
- **Filter by error type**: `*_LIMIT_REACHED`, `*_ALREADY_EXISTS`
- **Granular metrics**: Know which entity/error occurs most

### 3.5 Creating a New Exception

```java
package com.aegis.tests.orchestrator.exception;

import org.springframework.http.HttpStatus;

public class EnvironmentLimitReachedException extends BusinessException {

    private static final String ERROR_CODE = "ENVIRONMENT_LIMIT_REACHED";

    public EnvironmentLimitReachedException(Long testProjectId) {
        super(
                String.format("TestProject %d has reached the maximum limit of Environments", testProjectId),
                ERROR_CODE,
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }
}
```

---

## 4. API Error Response

### 4.1 Standard Format

All errors return `List<ErrorResponseDTO>`:

```json
[
  {
    "errorCode": "ERROR_CODE",
    "message": "Human readable message",
    "field": "fieldName"  
  }
]
```

### 4.2 Validation Error Codes

The `GlobalExceptionHandler` maps Bean Validation constraints to error codes:

| Constraint | Error Code |
|------------|------------|
| `@NotBlank`, `@NotNull`, `@NotEmpty` | `REQUIRED_FIELD` |
| `@Size`, `@Length` | `INVALID_FIELD_LENGTH` |
| `@Email` | `INVALID_EMAIL_FORMAT` |
| `@Pattern` | `INVALID_FORMAT` |
| `@Min`, `@Max` | `INVALID_VALUE_RANGE` |
| `@Positive`, `@Negative` | `INVALID_NUMBER` |
| `@Past`, `@Future` | `INVALID_DATE` |
| Others | `VALIDATION_ERROR` |

### 4.3 Response Examples

**Validation errors (400):**
```json
[
  {
    "errorCode": "REQUIRED_FIELD",
    "message": "Name is required",
    "field": "name"
  },
  {
    "errorCode": "INVALID_FIELD_LENGTH",
    "message": "Description must have at most 1000 characters",
    "field": "description"
  }
]
```

**Business error (409/422):**
```json
[
  {
    "errorCode": "TEST_PROJECT_LIMIT_REACHED",
    "message": "Project 10 has already reached the maximum limit of TestProjects"
  }
]
```

---

## 5. DTOs

### 5.1 Naming Convention

- Request: `*RequestDTO` (e.g., `CreateTestProjectRequestDTO`)
- Response: `*ResponseDTO` (e.g., `TestProjectResponseDTO`)

### 5.2 Immutability

Use Java Records for DTOs:

```java
public record CreateTestProjectRequestDTO(
    @NotBlank String name,
    String description
) {}
```

### 5.3 Null Safety

**Request DTOs**: Use Bean Validation (`@NotBlank`, `@NotNull`) - validated at runtime by Spring.

**Response DTOs**: Use `@NonNull` annotation only - IDE will show warnings at compile-time.

```java
// Response DTO - IDE warns if you pass null
public record ErrorResponseDTO(
    @NonNull String errorCode,  // IDE warning if null passed
    @NonNull String message,    // IDE warning if null passed  
    @Nullable String field      // OK to be null
) {
    // NO runtime validation needed - this is OUR code, not user input
}
```

**Rationale**: Response DTOs are constructed by our code, not external input. If we pass null, it's a programming error that should be caught during development via IDE warnings, not at runtime.

---

## 6. OpenAPI/Swagger Documentation

### 6.1 Keep It Concise

Use inline `@Schema` annotations:

```java
// ✅ Concise
@Schema(description = "Test module identifier name", example = "Receivables Test Suite", maxLength = 255)
String name;

// ❌ Avoid verbose multi-line
@Schema(
    description = "Test module identifier name",
    example = "Receivables Test Suite",
    requiredMode = Schema.RequiredMode.REQUIRED,
    maxLength = 255
)
String name;
```

### 6.2 Controller Annotations

```
@Operation(summary = "Create Test Project", description = "Short description here")
@ApiResponse(responseCode = "201", description = "Created successfully")
@ApiResponse(responseCode = "400", description = "Validation error",
        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
```

---

## 7. Project Structure

```
src/main/java/com/aegis/homolog/orchestrator/
├── config/           # Spring configurations
├── controller/       # REST controllers
├── exception/        # Exception classes and handlers
│   ├── BusinessException.java
│   ├── GlobalExceptionHandler.java
│   └── *Exception.java
├── model/
│   ├── dto/          # Request/Response DTOs
│   └── entity/       # JPA entities
├── repository/       # Spring Data repositories
└── services/         # Business logic

src/test/java/com/aegis/homolog/orchestrator/
├── controller/       # Controller tests
├── model/dto/        # DTO tests
├── repository/       # Repository tests
└── service/          # Service tests
```

---

## 8. Testing

### 8.1 Test for DTO Validation

Always test that required fields in response DTOs throw exceptions when null:

```java
@Test
void shouldThrowWhenErrorCodeIsNull() {
    assertThrows(NullPointerException.class,
        () -> new ErrorResponseDTO(null, "message", null));
}
```

### 8.2 Test Naming Convention

```java
@DisplayName("should return 400 Bad Request when name is blank")
void shouldReturn400WhenNameIsBlank() { }
```

---

## 9. Service Layer Patterns

### 9.1 Delegation Pattern (Recommended)

Each service should be responsible for its own domain. When an operation involves multiple domains, use **delegation** instead of putting all logic in one service.

**Example - TestProjectService creating a default Environment:**

```
// ✅ Correct - Delegation to EnvironmentService
@Service
public class TestProjectService {

    private final TestProjectRepository testProjectRepository;
    private final EnvironmentService environmentService;  // Delegates to Environment domain

    @Transactional
    public TestProjectResponseDTO create(...) {
        validateTestProjectLimit(projectId);
        validateNameUniqueness(projectId, request.name());

        TestProject savedProject = testProjectRepository.save(testProject);

        environmentService.createDefault(savedProject, userId);  // Delegation

        return TestProjectResponseDTO.fromEntity(savedProject);
    }
}
```

```
// ❌ Avoid - All logic in one service
@Service
public class TestProjectService {

    private final TestProjectRepository testProjectRepository;
    private final EnvironmentRepository environmentRepository;  // Wrong! Direct access to another domain's repository

    @Transactional
    public TestProjectResponseDTO create(...) {
        // ... TestProject logic ...
        
        // Environment logic embedded here - violates SRP
        Environment env = Environment.builder()...build();
        environmentRepository.save(env);
    }
}
```

### 9.2 Service Responsibility

| Responsibility | Belongs to | Example |
|----------------|------------|---------|
| Validate TestProject limit | `TestProjectService` | `validateTestProjectLimit()` |
| Validate TestProject name uniqueness | `TestProjectService` | `validateNameUniqueness()` |
| Create default Environment | `EnvironmentService` | `createDefault()` |
| Validate Environment limit | `EnvironmentService` | `validateEnvironmentLimit()` |

### 9.3 Benefits

- **Single Responsibility Principle (SRP)**: Each service handles its own domain
- **Testability**: Easy to mock dependencies in unit tests
- **Scalability**: Adding new features doesn't bloat existing services
- **Reusability**: `EnvironmentService.createDefault()` can be reused elsewhere

### 9.4 When to Use Domain Events (Advanced)

For more complex scenarios with multiple side effects, consider Domain Events:

```
// Publisher
applicationEventPublisher.publishEvent(new TestProjectCreatedEvent(savedProject));

// Listener
@EventListener
public void onTestProjectCreated(TestProjectCreatedEvent event) {
    createDefaultEnvironment(event.getTestProject());
}
```

**Use Domain Events when:**
- Multiple services need to react to an event
- You want complete decoupling
- Operations can be async

**Use Delegation when:**
- Simple, synchronous operations
- Clear parent-child relationship
- MVP/early stages of development

---

## 10. Validation Patterns

### 10.1 Validation Method Naming

Use `validate*` prefix for methods that throw exceptions:

```java
private void validateTestProjectLimit(Long projectId) {
    long count = testProjectRepository.countByProjectId(projectId);
    if (count >= MAX_TEST_PROJECTS_PER_PROJECT) {
        throw new TestProjectLimitReachedException(projectId);
    }
}

private void validateNameUniqueness(Long projectId, String name) {
    testProjectRepository.findByProjectIdAndName(projectId, name)
            .ifPresent(existing -> {
                throw new TestProjectNameAlreadyExistsException(name, projectId);
            });
}
```

### 10.2 Use Count for Limit Validation

Prefer `count*` methods over fetching all entities:

```java
// ✅ Efficient - only counts
long count = testProjectRepository.countByProjectId(projectId);

// ❌ Inefficient - fetches all entities
List<TestProject> projects = testProjectRepository.findByProjectId(projectId);
int count = projects.size();
```

