# API Response Standards

This document defines the response rules for all **Aegis Orchestrator** endpoints, ensuring consistency, traceability, and ease of consumption.

---

## Success Response Patterns (HTTP 2xx)

Success responses should be direct and reflect the created, modified, or retrieved resource.

### Success on Creation, Mutation, or Fetch (POST/PUT/PATCH/GET/DELETE)

| **Status Code** | **Primary Use** | **Response Body** |
| --- | --- | --- |
| **201 Created** | Return from a successful `POST`. | Must return the complete object (DTO) of the newly created resource, including its ID. |
| **200 OK** | Return from `PUT/PATCH` (update), `GET` (detail) | Returns the complete updated object (PUT) or the requested object (GET). |
| **204 No Content** | Return from a successful `DELETE`. | Response body must be **empty**. |

**Detail/Creation Structure (200/201):**

```json
{
  "id": "string",
  "name": "string",
  "plan": "string",
  "globalIdentifier": "string"
}
```

### Collections and Pagination (GET - Listing)

For endpoints that return lists (`GET /v1/org`), the return should use an envelope object to support pagination, sorting, and metadata.

| **Status Code** | **Primary Use** | **Response Body** |
| --- | --- | --- |
| **200 OK** | Resource listing (Organizations, Scenarios, etc.). | Must return the pagination object (Metadata + Items). |

**Standard Pagination Structure:**

```json
{
  "metadata": {
    "totalRecords": 42,
    "totalPages": 5,
    "currentPage": 1,
    "pageSize": 10,
    "hasNextPage": true
  },
  "items": [
    { "id": 1, "name": "Cerc Central" },
    { "id": 2, "name": "Nestle Brasil" }
  ]
}
```

---

## Error Response Patterns (HTTP 4xx)

This pattern covers all client failures, including authentication, authorization, and validation.

### Standard JSON Structure (Error Array)

To handle multiple field violations (`REQUIRED_FIELD`, `INVALID_FORMAT`), the response must be a **JSON Array**, where each object represents a specific error.

```json
[
  {
    "errorCode": "string",
    "message": "string",
    "field": "string"
  }
]
```

| **Field** | **Type** | **Description** |
| --- | --- | --- |
| `errorCode` | String | Internal standardized error code (Ex: `INVALID_FIELD_LENGTH`). Use `UPPER_SNAKE_CASE`. |
| `message` | String | User-friendly message in **English** that can be displayed directly to the user. |
| `field` | String | The DTO field name that caused the error. **Optional**, but recommended for 400 and 409 errors. |

### Error Code Naming Convention

Use **suffix-based naming** for error codes:

```
{ENTITY}_{ERROR_TYPE}
```

| **Suffix** | **Use Case** | **HTTP Status** |
|------------|--------------|-----------------|
| `*_LIMIT_REACHED` | Quantity limits | 422 |
| `*_ALREADY_EXISTS` | Duplicates | 409 |
| `*_NOT_FOUND` | Resource not found | 404 |
| `*_INVALID` | Invalid value | 400 |

**Examples:**

| Error Code | Description |
|------------|-------------|
| `TEST_PROJECT_LIMIT_REACHED` | TestProject limit per project reached |
| `TEST_PROJECT_NAME_ALREADY_EXISTS` | TestProject name already exists |
| `ENVIRONMENT_LIMIT_REACHED` | Environment limit reached |
| `ENVIRONMENT_NAME_ALREADY_EXISTS` | Environment name already exists |
| `SCENARIO_NOT_FOUND` | Scenario not found |

**Benefits:**
- **Filter by entity**: `TEST_PROJECT_*`
- **Filter by error type**: `*_LIMIT_REACHED`, `*_ALREADY_EXISTS`
- **Granular metrics**: Know which entity/error occurs most

### Validation Error Codes

The `GlobalExceptionHandler` maps Bean Validation constraints to error codes:

| **Constraint** | **Error Code** |
|----------------|----------------|
| `@NotBlank`, `@NotNull`, `@NotEmpty` | `REQUIRED_FIELD` |
| `@Size`, `@Length` | `INVALID_FIELD_LENGTH` |
| `@Email` | `INVALID_EMAIL_FORMAT` |
| `@Pattern` | `INVALID_FORMAT` |
| `@Min`, `@Max` | `INVALID_VALUE_RANGE` |
| `@Positive`, `@Negative` | `INVALID_NUMBER` |
| `@Past`, `@Future` | `INVALID_DATE` |
| Others | `VALIDATION_ERROR` |

### Status Code Categorization

| **Status Code** | **Category** | **Primary Use** |
| --- | --- | --- |
| **400 Bad Request** | Field Validation Error | Field Rule violations (RCs). Must return **all** violated field errors in the *array*. |
| **401 Unauthorized** | Authentication Error | Missing or invalid/malformed JWT token. |
| **403 Forbidden** | Permission/Governance Error | User doesn't have the required `role` (Ex: not `Admin`) or is inactive. |
| **409 Conflict** | Uniqueness Error | Attempt to create a resource that violates unique keys. |
| **422 Unprocessable Entity** | Business Rule Error | Business Rule violations (RNs). |

### Authorization Errors (must happen on all protected endpoints):

| **Code** | **Field(s) involved** | **Business Rule** | **Rationale** | **errorCode** |
| --- | --- | --- | --- | --- |
| RNxx.1 | `[Header] authorization` | Must be a valid and non-expired JWT token. | Fundamental security. | `INVALID_AUTH_TOKEN` |
| **RNxx.2** | `[Header] authorization` | The user ID (extracted from token) must be an active user on the Auth platform. | Ensures the creator user is enabled (not deactivated in IAM). | `USER_ACCOUNT_INACTIVE` |

### Response Examples

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

**Business error (422):**
```json
[
  {
    "errorCode": "TEST_PROJECT_LIMIT_REACHED",
    "message": "Project 10 has already reached the maximum limit of TestProjects"
  }
]
```

**Conflict error (409):**
```json
[
  {
    "errorCode": "TEST_PROJECT_NAME_ALREADY_EXISTS",
    "message": "TestProject with name 'My Project' already exists in project 10",
    "field": "name"
  }
]
```

---

## Request Patterns (Headers and Consumption)

| **Padrão** | **Regra** | **Racional** |
| --- | --- | --- |
| **Autenticação** | Todas as rotas autenticadas devem esperar o header `Authorization: Bearer <token>`. | Padrão OAuth2/JWT. |
| **Content Type** | O corpo das requisições `POST`, `PUT` e `PATCH` deve ser `Content-Type: application/json`. | Padronização na comunicação entre Frontend e Spring. |
| **Aceitação** | O header `Accept` deve ser sempre `application/json` (a menos que seja uma rota de download de arquivo/relatório). | Padronização na resposta. |
| **Timezones** | Todas as datas e horas (incluindo `createdAt` no JPA) devem ser persistidas em **UTC**. | Garante consistência global e evita problemas de fuso horário em logs e execução. |