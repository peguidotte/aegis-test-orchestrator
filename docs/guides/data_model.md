# **Data Model**

This document describes the JPA entities of the **aegis-homolog-orchestrator** project and their relationships.

> ⚠️ This document must be kept updated as the code evolves.

---

## Index

1. [Core Entities](#core-entities)
   - [TestProject](#testproject)
   - [Environment](#environment)
   - [Specification](#specification)
2. [API Modeling](#api-modeling)
   - [Domain](#domain)
   - [ApiCall](#apicall)
3. [Authentication](#authentication)
   - [AuthProfile](#authprofile)
   - [AuthCredentials](#authcredentials)
4. [Tagging System](#tagging-system)
   - [Tag](#tag)
5. [Entity Relationships Overview](#entity-relationships-overview)
6. [Enumerations](#enumerations)
7. [Base Classes](#base-classes)

---

## Core Entities

## TestProject

**Table:** `test_projects`

**Package:** `com.aegis.tests.orchestrator.testproject`

Root container for the Aegis Tests module. Represents a test project linked to a Core Project.

### Fields

| Field | Type | Nullable | Description | Constraint |
|-------|------|----------|-------------|------------|
| `test_project_id` | `BIGINT` | ❌ | PK, auto-increment | `PRIMARY KEY` |
| `project_id` | `BIGINT` | ❌ | FK to Core Project (external) | `INDEX` |
| `name` | `VARCHAR(255)` | ❌ | Test project identifier name | `UNIQUE(project_id, name)` |
| `description` | `VARCHAR(1000)` | ✅ | Scope description | - |
| `created_at` | `TIMESTAMP` | ❌ | Creation date (UTC) | - |
| `updated_at` | `TIMESTAMP` | ❌ | Last update date (UTC) | - |
| `created_by` | `VARCHAR(64)` | ❌ | Creator user | - |
| `last_updated_by` | `VARCHAR(64)` | ❌ | Last modifier user | - |

### Indexes

- `idx_test_project_project_id` → `project_id`
- `idx_test_project_name` → `name`
- `uk_test_project_project_name` → `(project_id, name)` UNIQUE

### Relationships

- **1:N** → `Environment` (one TestProject has many Environments)
- **1:N** → `Specification` (one TestProject has many Specifications)
- **1:N** → `Domain` (one TestProject has many Domains)
- **1:N** → `AuthProfile` (one TestProject has many AuthProfiles)
- **N:M** → `Tag` (via join table `test_project_tags`)

### Business Rules

| Code | Rule |
|------|------|
| RN10.01.2 | Each Core Project can have at most 1 TestProject (MVP) |
| RN10.01.3 | Name must be unique within the same project |

---

## Environment

**Table:** `environments`

**Package:** `com.aegis.tests.orchestrator.environment`

Represents a test execution context (e.g., DEV, STAGING, PROD).

### Fields

| Field | Type | Nullable | Description | Constraint |
|-------|------|----------|-------------|------------|
| `environment_id` | `BIGINT` | ❌ | PK, auto-increment | `PRIMARY KEY` |
| `test_project_id` | `BIGINT` | ❌ | FK to TestProject | `FOREIGN KEY` |
| `name` | `VARCHAR(100)` | ❌ | Environment name | `UNIQUE(test_project_id, name)` |
| `description` | `VARCHAR(500)` | ✅ | Environment description | - |
| `is_default` | `BOOLEAN` | ❌ | Indicates if this is the default environment | - |
| `created_at` | `TIMESTAMP` | ❌ | Creation date (UTC) | - |
| `updated_at` | `TIMESTAMP` | ❌ | Last update date (UTC) | - |
| `created_by` | `VARCHAR(64)` | ❌ | Creator user | - |
| `last_updated_by` | `VARCHAR(64)` | ❌ | Last modifier user | - |

### Indexes

- `idx_environment_test_project_id` → `test_project_id`
- `idx_environment_name` → `name`
- `uk_environment_project_name` → `(test_project_id, name)` UNIQUE

### Relationships

- **N:1** → `TestProject` (each Environment belongs to a TestProject)
- **N:M** → `ApiCall` (via join table `api_call_environments`)

### Automatic Behaviors

- When a `TestProject` is created, an `Environment` named **"Default"** is automatically created with `is_default = true`.

---

## Specification

**Table:** `specifications`

**Package:** `com.aegis.tests.orchestrator.specification`

Represents a test specification that can be manually created or AI-generated.

### Fields

| Field | Type | Nullable | Description | Constraint |
|-------|------|----------|-------------|------------|
| `specification_id` | `BIGINT` | ❌ | PK, auto-increment | `PRIMARY KEY` |
| `test_project_id` | `BIGINT` | ❌ | FK to TestProject | `FOREIGN KEY` |
| `title` | `VARCHAR(255)` | ❌ | Specification title | - |
| `description` | `TEXT` | ✅ | Detailed description | - |
| `status` | `VARCHAR(50)` | ❌ | Current specification status | - |
| `created_at` | `TIMESTAMP` | ❌ | Creation date (UTC) | - |
| `updated_at` | `TIMESTAMP` | ❌ | Last update date (UTC) | - |
| `created_by` | `VARCHAR(64)` | ❌ | Creator user | - |
| `last_updated_by` | `VARCHAR(64)` | ❌ | Last modifier user | - |

### Indexes

- `idx_specification_test_project_id` → `test_project_id`
- `idx_specification_status` → `status`

### Relationships

- **N:1** → `TestProject` (each Specification belongs to a TestProject)
- **N:M** → `Tag` (via join table `specification_tags`)
- **N:M** → `ApiCall` (via join table `specification_api_calls`)
- **N:M** → `Domain` (via join table `specification_domains`)

### Status Lifecycle

Specifications follow a state machine with the following valid transitions:

```
CREATED → PROCESSING
PROCESSING → PLANNING | ERROR
PLANNING → PLANNED | ERROR
PLANNED → WAITING_APPROVAL | ERROR
WAITING_APPROVAL → APPROVED | APPROVED_WITH_EDITS | REJECTED | ERROR
APPROVED → PROCESSING | ERROR
APPROVED_WITH_EDITS → PROCESSING | ERROR
REJECTED → PROCESSING | ERROR
PROCESSING → GENERATING_TESTS | ERROR
GENERATING_TESTS → TESTING_TESTS | ERROR
TESTING_TESTS → TESTS_GENERATED | GENERATING_TESTS | ERROR
TESTS_GENERATED → (final state)
ERROR → PROCESSING (retry)
```

See `SpecificationStatus` enum for all valid states and `SpecificationStatusTransitions` for validation logic.

---

## API Modeling

## Domain

**Table:** `domains`

**Package:** `com.aegis.tests.orchestrator.domain`

Represents a business domain used to organize API calls logically (e.g., "Users", "Orders", "Payments").

### Fields

| Field | Type | Nullable | Description | Constraint |
|-------|------|----------|-------------|------------|
| `domain_id` | `BIGINT` | ❌ | PK, auto-increment | `PRIMARY KEY` |
| `test_project_id` | `BIGINT` | ❌ | FK to TestProject | `FOREIGN KEY` |
| `name` | `VARCHAR(100)` | ❌ | Domain name | `UNIQUE(test_project_id, name)` |
| `description` | `VARCHAR(500)` | ✅ | Domain description | - |
| `created_at` | `TIMESTAMP` | ❌ | Creation date (UTC) | - |
| `updated_at` | `TIMESTAMP` | ❌ | Last update date (UTC) | - |
| `created_by` | `VARCHAR(64)` | ❌ | Creator user | - |
| `last_updated_by` | `VARCHAR(64)` | ❌ | Last modifier user | - |

### Indexes

- `idx_domain_test_project_id` → `test_project_id`
- `uk_domain_project_name` → `(test_project_id, name)` UNIQUE

### Relationships

- **N:1** → `TestProject` (each Domain belongs to a TestProject)
- **1:N** → `ApiCall` (one Domain has many ApiCalls)
- **N:M** → `Specification` (via join table `specification_domains`)
- **N:M** → `Tag` (via join table `domain_tags`)

---

## ApiCall

**Table:** `api_calls`

**Package:** `com.aegis.tests.orchestrator.apicall`

Represents an API endpoint with its complete specification (method, path, headers, body, auth, etc.).

### Fields

| Field | Type | Nullable | Description | Constraint |
|-------|------|----------|-------------|------------|
| `api_call_id` | `BIGINT` | ❌ | PK, auto-increment | `PRIMARY KEY` |
| `test_project_id` | `BIGINT` | ❌ | FK to TestProject | `FOREIGN KEY` |
| `domain_id` | `BIGINT` | ✅ | FK to Domain (optional) | `FOREIGN KEY` |
| `auth_profile_id` | `BIGINT` | ✅ | FK to AuthProfile (optional) | `FOREIGN KEY` |
| `name` | `VARCHAR(255)` | ❌ | API call name | - |
| `description` | `VARCHAR(1000)` | ✅ | Detailed description | - |
| `http_method` | `VARCHAR(10)` | ❌ | HTTP method (GET, POST, etc.) | - |
| `path` | `VARCHAR(500)` | ❌ | Endpoint path | - |
| `headers` | `TEXT` | ✅ | JSON with headers | - |
| `request_body` | `TEXT` | ✅ | JSON with request body schema | - |
| `created_at` | `TIMESTAMP` | ❌ | Creation date (UTC) | - |
| `updated_at` | `TIMESTAMP` | ❌ | Last update date (UTC) | - |
| `created_by` | `VARCHAR(64)` | ❌ | Creator user | - |
| `last_updated_by` | `VARCHAR(64)` | ❌ | Last modifier user | - |

### Indexes

- `idx_api_call_test_project_id` → `test_project_id`
- `idx_api_call_domain_id` → `domain_id`
- `idx_api_call_auth_profile_id` → `auth_profile_id`
- `idx_api_call_http_method` → `http_method`

### Relationships

- **N:1** → `TestProject` (each ApiCall belongs to a TestProject)
- **N:1** → `Domain` (each ApiCall may belong to a Domain)
- **N:1** → `AuthProfile` (each ApiCall may use an AuthProfile)
- **N:M** → `Environment` (via join table `api_call_environments` with base_url)
- **N:M** → `Specification` (via join table `specification_api_calls`)
- **N:M** → `Tag` (via join table `api_call_tags`)

### Join Table: api_call_environments

This table stores the base URL for each ApiCall in each Environment.

| Field | Type | Description |
|-------|------|-------------|
| `api_call_id` | `BIGINT` | FK to ApiCall |
| `environment_id` | `BIGINT` | FK to Environment |
| `base_url` | `VARCHAR(500)` | Base URL for this environment |

**Primary Key:** `(api_call_id, environment_id)`

---

## Authentication

## AuthProfile

**Table:** `auth_profiles`

**Package:** `com.aegis.tests.orchestrator.authprofile`

Represents an authentication profile that defines how to authenticate with an API.

### Fields

| Field | Type | Nullable | Description | Constraint |
|-------|------|----------|-------------|------------|
| `auth_profile_id` | `BIGINT` | ❌ | PK, auto-increment | `PRIMARY KEY` |
| `test_project_id` | `BIGINT` | ❌ | FK to TestProject | `FOREIGN KEY` |
| `name` | `VARCHAR(100)` | ❌ | Profile name | `UNIQUE(test_project_id, name)` |
| `description` | `VARCHAR(500)` | ✅ | Profile description | - |
| `auth_type` | `VARCHAR(50)` | ❌ | Authentication type | - |
| `created_at` | `TIMESTAMP` | ❌ | Creation date (UTC) | - |
| `updated_at` | `TIMESTAMP` | ❌ | Last update date (UTC) | - |
| `created_by` | `VARCHAR(64)` | ❌ | Creator user | - |
| `last_updated_by` | `VARCHAR(64)` | ❌ | Last modifier user | - |

### Indexes

- `idx_auth_profile_test_project_id` → `test_project_id`
- `uk_auth_profile_project_name` → `(test_project_id, name)` UNIQUE

### Relationships

- **N:1** → `TestProject` (each AuthProfile belongs to a TestProject)
- **1:N** → `AuthCredentials` (one AuthProfile has many Credentials per environment)
- **1:N** → `ApiCall` (one AuthProfile can be used by many ApiCalls)
- **N:M** → `Tag` (via join table `auth_profile_tags`)

---

## AuthCredentials

**Table:** `auth_credentials`

**Package:** `com.aegis.tests.orchestrator.authprofile`

Stores encrypted credentials for an AuthProfile in a specific Environment.

### Fields

| Field | Type | Nullable | Description | Constraint |
|-------|------|----------|-------------|------------|
| `auth_credentials_id` | `BIGINT` | ❌ | PK, auto-increment | `PRIMARY KEY` |
| `auth_profile_id` | `BIGINT` | ❌ | FK to AuthProfile | `FOREIGN KEY` |
| `environment_id` | `BIGINT` | ❌ | FK to Environment | `FOREIGN KEY` |
| `auth_location` | `VARCHAR(50)` | ❌ | Where auth is placed (HEADER, QUERY, BODY) | - |
| `encrypted_credentials` | `TEXT` | ❌ | Encrypted JSON with credentials | - |
| `created_at` | `TIMESTAMP` | ❌ | Creation date (UTC) | - |
| `updated_at` | `TIMESTAMP` | ❌ | Last update date (UTC) | - |
| `created_by` | `VARCHAR(64)` | ❌ | Creator user | - |
| `last_updated_by` | `VARCHAR(64)` | ❌ | Last modifier user | - |

### Indexes

- `idx_auth_credentials_auth_profile_id` → `auth_profile_id`
- `idx_auth_credentials_environment_id` → `environment_id`
- `uk_auth_credentials_profile_env` → `(auth_profile_id, environment_id)` UNIQUE

### Relationships

- **N:1** → `AuthProfile` (each Credential belongs to an AuthProfile)
- **N:1** → `Environment` (each Credential is specific to an Environment)

### Security

- Credentials are **encrypted** using `EncryptionService` before storage
- Decryption happens only when needed for test execution
- The encryption key is managed externally (not in database)

---

## Tagging System

## Tag

**Table:** `tags`

**Package:** `com.aegis.tests.orchestrator.shared.tag`

Represents a tag that can be applied to multiple entities for organization and filtering.

### Fields

| Field | Type | Nullable | Description | Constraint |
|-------|------|----------|-------------|------------|
| `tag_id` | `BIGINT` | ❌ | PK, auto-increment | `PRIMARY KEY` |
| `test_project_id` | `BIGINT` | ❌ | FK to TestProject | `FOREIGN KEY` |
| `name` | `VARCHAR(50)` | ❌ | Tag name | `UNIQUE(test_project_id, name)` |
| `color` | `VARCHAR(7)` | ✅ | Hex color code (e.g., #FF5733) | - |
| `created_at` | `TIMESTAMP` | ❌ | Creation date (UTC) | - |
| `updated_at` | `TIMESTAMP` | ❌ | Last update date (UTC) | - |
| `created_by` | `VARCHAR(64)` | ❌ | Creator user | - |
| `last_updated_by` | `VARCHAR(64)` | ❌ | Last modifier user | - |

### Indexes

- `idx_tag_test_project_id` → `test_project_id`
- `uk_tag_project_name` → `(test_project_id, name)` UNIQUE

### Relationships

Tags can be applied to multiple entity types:

- **N:M** → `TestProject` (via join table `test_project_tags`)
- **N:M** → `Specification` (via join table `specification_tags`)
- **N:M** → `ApiCall` (via join table `api_call_tags`)
- **N:M** → `Domain` (via join table `domain_tags`)
- **N:M** → `AuthProfile` (via join table `auth_profile_tags`)

---

## Entity Relationships Overview

### Complete ER Diagram

```
┌─────────────────────┐
│    TestProject      │
├─────────────────────┤
│ test_project_id (PK)│───┐
│ project_id          │   │
│ name                │   │
│ description         │   │
└─────────────────────┘   │
         │                │
         │ 1:N            │
         ▼                │
┌─────────────────────┐   │
│    Environment      │   │
├─────────────────────┤   │
│ environment_id (PK) │   │
│ test_project_id (FK)│◄──┤
│ name                │   │
│ is_default          │   │
└─────────────────────┘   │
         │                │
         │ N:M            │
         ▼                │
┌─────────────────────┐   │
│      ApiCall        │   │
├─────────────────────┤   │
│ api_call_id (PK)    │   │
│ test_project_id (FK)│◄──┤
│ domain_id (FK)      │   │
│ auth_profile_id (FK)│   │
│ name                │   │
│ http_method         │   │
│ path                │   │
│ headers             │   │
│ request_body        │   │
└─────────────────────┘   │
         │                │
         │ N:1            │
         ▼                │
┌─────────────────────┐   │
│       Domain        │   │
├─────────────────────┤   │
│ domain_id (PK)      │   │
│ test_project_id (FK)│◄──┤
│ name                │   │
│ description         │   │
└─────────────────────┘   │
                          │
┌─────────────────────┐   │
│   Specification     │   │
├─────────────────────┤   │
│ specification_id(PK)│   │
│ test_project_id (FK)│◄──┤
│ title               │   │
│ description         │   │
│ status              │   │
└─────────────────────┘   │
         │                │
         │ N:M            │
         │ (spec_api_     │
         │  calls)        │
         └────────────────┘
                          │
┌─────────────────────┐   │
│    AuthProfile      │   │
├─────────────────────┤   │
│ auth_profile_id (PK)│   │
│ test_project_id (FK)│◄──┤
│ name                │   │
│ auth_type           │   │
└─────────────────────┘   │
         │                │
         │ 1:N            │
         ▼                │
┌─────────────────────┐   │
│  AuthCredentials    │   │
├─────────────────────┤   │
│ auth_credentials_id │   │
│ auth_profile_id (FK)│   │
│ environment_id (FK) │   │
│ auth_location       │   │
│ encrypted_creds     │   │
└─────────────────────┘   │
                          │
┌─────────────────────┐   │
│        Tag          │   │
├─────────────────────┤   │
│ tag_id (PK)         │   │
│ test_project_id (FK)│◄──┘
│ name                │
│ color               │
└─────────────────────┘
         │
         │ N:M (multiple join tables)
         └─► TestProject, Specification, 
             ApiCall, Domain, AuthProfile
```

### Key Relationships

| From | To | Type | Description |
|------|-----|------|-------------|
| TestProject | Environment | 1:N | One project has many environments |
| TestProject | Specification | 1:N | One project has many specs |
| TestProject | Domain | 1:N | One project has many domains |
| TestProject | AuthProfile | 1:N | One project has many auth profiles |
| TestProject | ApiCall | 1:N | One project has many API calls |
| TestProject | Tag | 1:N | One project has many tags |
| Domain | ApiCall | 1:N | One domain groups many API calls |
| AuthProfile | AuthCredentials | 1:N | One profile has credentials per environment |
| AuthProfile | ApiCall | 1:N | One profile can be used by many API calls |
| Environment | ApiCall | N:M | Each API call has base URLs per environment |
| Environment | AuthCredentials | 1:N | One environment has many credentials |
| Specification | ApiCall | N:M | Many-to-many relationship |
| Specification | Domain | N:M | Many-to-many relationship |
| Specification | Tag | N:M | Many-to-many relationship |
| ApiCall | Tag | N:M | Many-to-many relationship |
| Domain | Tag | N:M | Many-to-many relationship |
| AuthProfile | Tag | N:M | Many-to-many relationship |

---

## Enumerations

### SpecificationStatus

**Package:** `com.aegis.tests.orchestrator.specification.enums`

Represents the lifecycle status of a Specification.

```java
public enum SpecificationStatus {
    CREATED,              // Initial state when created
    PROCESSING,           // AI is processing
    PLANNING,             // AI is planning tests
    PLANNED,              // AI has planned tests
    WAITING_APPROVAL,     // Waiting for user approval
    APPROVED,             // User approved plan
    APPROVED_WITH_EDITS,  // User approved with modifications
    REJECTED,             // User rejected plan
    GENERATING_TESTS,     // AI is generating test code
    TESTING_TESTS,        // AI is testing generated tests
    TESTS_GENERATED,      // Tests successfully generated
    ERROR                 // Error occurred
}
```

### AuthType

**Package:** `com.aegis.tests.orchestrator.authprofile.enums`

Defines the type of authentication.

```java
public enum AuthType {
    BASIC,           // Basic authentication
    BEARER_TOKEN,    // Bearer token
    API_KEY,         // API Key
    OAUTH2           // OAuth 2.0
}
```

### AuthLocation

**Package:** `com.aegis.tests.orchestrator.authprofile.enums`

Defines where the authentication information should be placed.

```java
public enum AuthLocation {
    HEADER,    // In HTTP headers
    QUERY,     // In query parameters
    BODY       // In request body
}
```

### HttpMethod

**Package:** `com.aegis.tests.orchestrator.apicall.enums`

Represents HTTP methods.

```java
public enum HttpMethod {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE,
    HEAD,
    OPTIONS
}
```

---

## Base Classes

### AuditableEntity

**Package:** `com.aegis.tests.orchestrator.shared.entity`

All entities extend `AuditableEntity`, which provides audit fields:

```java
@MappedSuperclass
@Getter
@Setter
public abstract class AuditableEntity {
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Column(name = "created_by", nullable = false, length = 64)
    private String createdBy;
    
    @Column(name = "last_updated_by", nullable = false, length = 64)
    private String lastUpdatedBy;
    
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        // TODO: Get from SecurityContext when authentication is implemented
        this.createdBy = "SYSTEM";
        this.lastUpdatedBy = "SYSTEM";
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
        // TODO: Get from SecurityContext when authentication is implemented
        this.lastUpdatedBy = "SYSTEM";
    }
}
```

### Features

- **Automatic timestamps**: `createdAt` and `updatedAt` are managed automatically
- **User tracking**: `createdBy` and `lastUpdatedBy` track who made changes
- **Immutable creation**: `createdAt` cannot be updated after entity creation
- **JPA Lifecycle Hooks**: Uses `@PrePersist` and `@PreUpdate`

---

## Architecture Notes

### Package-by-Feature Organization

The project follows **package-by-feature** structure instead of traditional layer-based packaging:

```
com.aegis.tests.orchestrator/
├── testproject/
│   ├── TestProject.java (entity)
│   ├── TestProjectRepository.java
│   ├── TestProjectService.java
│   ├── TestProjectController.java
│   └── dto/
├── environment/
│   ├── Environment.java (entity)
│   ├── EnvironmentRepository.java
│   └── EnvironmentService.java
├── specification/
│   ├── Specification.java (entity)
│   ├── SpecificationRepository.java
│   ├── SpecificationService.java
│   ├── SpecificationController.java
│   ├── dto/
│   └── enums/
├── domain/
├── apicall/
├── authprofile/
└── shared/
    ├── entity/ (AuditableEntity)
    ├── exception/
    ├── security/ (EncryptionService)
    └── tag/
```

### Benefits

- **High cohesion**: Related code stays together
- **Better discoverability**: Easy to find all code related to a feature
- **Reduced coupling**: Features are more independent
- **Easier to refactor**: Changes are localized to feature packages

---

## Security Considerations

1. **Encrypted Credentials**: All authentication credentials are encrypted using AES-256
2. **Audit Trail**: All entities track creation and modification metadata
3. **Unique Constraints**: Prevent duplicate data within projects
4. **Foreign Key Constraints**: Maintain referential integrity
5. **Indexes**: Optimize query performance on frequently accessed fields

---

## Future Enhancements (Post-MVP)

- **Feature**: Test scenarios storage (for AI-generated tests)
- **Scenario**: Individual test scenarios within features
- **Step**: Granular steps within scenarios
- **ExecutionResult**: Store test execution results
- **TestRun**: Track test execution sessions
- **Variable**: Global and environment-specific variables
- **Webhook**: Event notifications for test events

---

*Last Updated: 2026-01-30*  
*Document Version: 2.0*
