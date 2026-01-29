# **Modelo de Dados**

Este documento descreve as entidades JPA do projeto **aegis-homolog-orchestrator** e seus relacionamentos.

> ⚠️ É importante manter este documento atualizado conforme o código evolui.

---

## Índice

1. [TestProject](#testproject)
2. [Environment](#environment)

---

## TestProject

**Tabela:** `test_projects`

Container raiz do módulo Aegis Tests. Representa um projeto de testes vinculado a um Projeto Core.

### Campos

| Campo | Tipo | Nullable | Descrição | Constraint |
|-------|------|----------|-----------|------------|
| `test_project_id` | `BIGINT` | ❌ | PK, auto-increment | `PRIMARY KEY` |
| `project_id` | `BIGINT` | ❌ | FK para Projeto Core (externo) | `INDEX` |
| `name` | `VARCHAR(255)` | ❌ | Nome identificador do módulo | `UNIQUE(project_id, name)` |
| `description` | `VARCHAR(1000)` | ✅ | Descrição do escopo | - |
| `created_at` | `TIMESTAMP` | ❌ | Data de criação (UTC) | - |
| `updated_at` | `TIMESTAMP` | ❌ | Data de atualização (UTC) | - |
| `created_by` | `VARCHAR(64)` | ❌ | Usuário criador | - |
| `last_updated_by` | `VARCHAR(64)` | ❌ | Último usuário que alterou | - |

### Índices

- `idx_test_project_project_id` → `project_id`
- `idx_test_project_name` → `name`
- `uk_test_project_project_name` → `(project_id, name)` UNIQUE

### Relacionamentos

- **1:N** → `Environment` (um TestProject possui vários Environments)

### Regras de Negócio

| Código | Regra |
|--------|-------|
| RN10.01.2 | Cada Projeto Core pode ter no máximo 1 TestProject (MVP) |
| RN10.01.3 | Nome deve ser único dentro do mesmo projeto |

---

## Environment

**Tabela:** `environments`

Representa um contexto de execução de testes (ex: DEV, STAGING, PROD).

### Campos

| Campo | Tipo | Nullable | Descrição | Constraint |
|-------|------|----------|-----------|------------|
| `environment_id` | `BIGINT` | ❌ | PK, auto-increment | `PRIMARY KEY` |
| `test_project_id` | `BIGINT` | ❌ | FK para TestProject | `FOREIGN KEY` |
| `name` | `VARCHAR(100)` | ❌ | Nome do ambiente | `UNIQUE(test_project_id, name)` |
| `description` | `VARCHAR(500)` | ✅ | Descrição do ambiente | - |
| `is_default` | `BOOLEAN` | ❌ | Indica se é o ambiente padrão | - |
| `created_at` | `TIMESTAMP` | ❌ | Data de criação (UTC) | - |
| `updated_at` | `TIMESTAMP` | ❌ | Data de atualização (UTC) | - |
| `created_by` | `VARCHAR(64)` | ❌ | Usuário criador | - |
| `last_updated_by` | `VARCHAR(64)` | ❌ | Último usuário que alterou | - |

### Índices

- `idx_environment_test_project_id` → `test_project_id`
- `idx_environment_name` → `name`
- `uk_environment_project_name` → `(test_project_id, name)` UNIQUE

### Relacionamentos

- **N:1** → `TestProject` (cada Environment pertence a um TestProject)

### Comportamentos Automáticos

- Quando um `TestProject` é criado, um `Environment` chamado **"Default"** é criado automaticamente com `is_default = true`.

---

## Diagrama ER (Simplificado)

```
┌─────────────────────┐       ┌─────────────────────┐
│    TestProject      │       │    Environment      │
├─────────────────────┤       ├─────────────────────┤
│ test_project_id (PK)│───┐   │ environment_id (PK) │
│ project_id          │   │   │ test_project_id (FK)│──┐
│ name                │   └──►│ name                │  │
│ description         │       │ description         │  │
│ created_at          │       │ is_default          │  │
│ updated_at          │       │ created_at          │  │
│ created_by          │       │ updated_at          │  │
│ last_updated_by     │       │ created_by          │  │
└─────────────────────┘       │ last_updated_by     │  │
                              └─────────────────────┘  │
                                        │              │
                                        └──────────────┘
```

---

## Classe Base: AuditableEntity

Todas as entidades herdam de `AuditableEntity`, que fornece os campos de auditoria:

```java
@MappedSuperclass
public abstract class AuditableEntity {
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String lastUpdatedBy;
}
```

- `@PrePersist`: Define `createdAt` e `updatedAt` automaticamente
- `@PreUpdate`: Atualiza `updatedAt` automaticamente
