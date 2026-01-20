> **COMO:** Administrador ou Colaborador
>
> **QUERO:** Criar uma especificação manual de teste de API
>
> **PARA:** Fornecer contexto estruturado para geração automatizada de testes via IA.

---

## Contexto

Uma **Specification** (Especificação de Teste) define **o que testar** em um endpoint específico. Não é apenas "testar se cria invoice", mas sim **garantir cobertura regressiva completa para este endpoint**.

A Specification é a **fonte de verdade funcional** usada pela IA para:
- Planejar cenários de teste
- Definir STEPs (KarateDSL)
- Gerar testes automatizados

A Specification **não gera testes automaticamente**, ela apenas registra:
- Intenção
- Contexto
- Regras
- Nível de autonomia da IA

---

## Modalidades de Input

A Specification suporta **duas modalidades de criação**:

### 1. Input MANUAL
O usuário fornece diretamente as informações do endpoint:
- HTTP Method
- Route Definition (path)
- Request/Response Examples

**Caso de uso:** Quando o endpoint ainda não está catalogado no sistema.

### 2. Input via API_CALL
O usuário referencia um `ApiCall` existente do catálogo de endpoints:
- O sistema já possui método HTTP, rota, domínio, baseUrl
- Evita duplicação de informações

**Caso de uso:** Quando o endpoint já foi catalogado (via API docs, importação, ou cadastro anterior).

---

## Endpoint

<aside>
➡️

**POST** `/v1/test-projects/{testProjectId}/specifications`

</aside>

Cria uma **Specification** de teste dentro de um `TestProject`.

---

## Layouts

### Request

| Campo | Tipo | Descrição | Exemplo | Req. |
| --- | --- | --- | --- | --- |
| `inputType` | Enum | Modalidade de input | `MANUAL` ou `API_CALL` | Obrigatório |
| `name` | String (255) | Nome da especificação | `Criar Duplicata` | Obrigatório |
| `description` | String (1000) | Descrição funcional | `Fluxo de criação...` | Opcional |
| `testObjective` | String (2000) | Objetivo detalhado do teste | `Garantir que...` | Obrigatório |
| **Campos para MANUAL** |
| `method` | Enum (HTTP) | Método HTTP principal | `POST` | Condicional* |
| `path` | String (500) | Path relativo da API | `/v1/duplicates` | Condicional* |
| `requestExample` | Text | Exemplo de payload | `{ "amount": 100 }` | Opcional |
| **Campos para API_CALL** |
| `apiCallId` | Long | ID do ApiCall do catálogo | `42` | Condicional** |
| **Campos Comuns** |
| `domainId` | Long | Domínio semântico da API | `12` | Opcional |
| `requiresAuth` | Boolean | Indica se a rota exige autenticação | `true` | Obrigatório |
| `authProfileId` | Long | Perfil de autenticação | `5` | Condicional*** |
| `environmentId` | Long | Ambiente alvo da spec | `3` | Obrigatório |
| `supportingApiCallIds` | Set\<Long\> | IDs de endpoints auxiliares | `[10, 11, 12]` | Opcional |
| `approveBeforeGeneration` | Boolean | Exige aprovação antes da IA gerar | `true` | Obrigatório |

> \* Obrigatório se `inputType == MANUAL`
> 
> \*\* Obrigatório se `inputType == API_CALL`
> 
> \*\*\* Obrigatório se `requiresAuth == true`

#### Exemplo Request (MANUAL)

```json
{
  "inputType": "MANUAL",
  "name": "Criar Duplicata",
  "description": "Fluxo de criação de duplicatas com validações de negócio",
  "testObjective": "Garantir que o endpoint de criação de duplicatas valide corretamente os campos obrigatórios, regras de negócio de valor mínimo/máximo, e retorne os códigos HTTP apropriados",
  "method": "POST",
  "path": "/v1/duplicates",
  "requestExample": {
    "amount": 1000,
    "dueDate": "2026-02-10"
  },
  "domainId": 12,
  "requiresAuth": true,
  "authProfileId": 5,
  "environmentId": 3,
  "supportingApiCallIds": [10, 15],
  "approveBeforeGeneration": true
}
```

#### Exemplo Request (API_CALL)

```json
{
  "inputType": "API_CALL",
  "name": "Testes de Criação de Invoice",
  "description": "Cobertura completa do endpoint de criação de invoices",
  "testObjective": "Validar todos os cenários de criação de invoice incluindo validações de campo, regras de negócio e integrações",
  "apiCallId": 42,
  "requiresAuth": true,
  "authProfileId": 5,
  "environmentId": 3,
  "supportingApiCallIds": [43, 44, 45],
  "approveBeforeGeneration": false
}
```

### Response

| Campo | Tipo | Descrição | Exemplo | Req. |
| --- | --- | --- | --- | --- |
| `id` | Long | ID da Specification | `9001` | Obrigatório |
| `inputType` | Enum | Modalidade usada | `MANUAL` | Obrigatório |
| `name` | String | Nome da specification | `Criar Duplicata` | Obrigatório |
| `testObjective` | String | Objetivo do teste | `Garantir que...` | Obrigatório |
| `method` | Enum | Método HTTP (resolvido) | `POST` | Obrigatório |
| `path` | String | Path (resolvido) | `/v1/duplicates` | Obrigatório |
| `status` | Enum | Status da spec | `CREATED` | Obrigatório |
| `createdAt` | Timestamp | Data de criação | `2026-01-13T10:00:00Z` | Obrigatório |
| `updatedAt` | Timestamp | Última atualização | `2026-01-13T10:00:00Z` | Obrigatório |

### SpecStatus (Domínio)

- `CREATED` - Criada, aguardando processamento
- `WAITING_APPROVAL` - Aguardando aprovação manual
- `APPROVED` - Aprovada para geração
- `REJECTED` - Rejeitada
- `GENERATING_TESTS` - IA gerando testes
- `TESTS_GENERATED` - Testes gerados com sucesso

```json
{
  "id": 9001,
  "inputType": "MANUAL",
  "name": "Criar Duplicata",
  "testObjective": "Garantir que o endpoint...",
  "method": "POST",
  "path": "/v1/duplicates",
  "status": "WAITING_APPROVAL",
  "createdAt": "2026-01-13T10:00:00Z",
  "updatedAt": "2026-01-13T10:00:00Z"
}
```

---

## Entidades Relacionadas

### BaseUrl (NOVA)

Representa uma URL base reutilizável, associada a um ambiente.

| Campo | Tipo | Descrição |
| --- | --- | --- |
| `id` | Long | Identificador único |
| `identifier` | String | Nome identificador (ex: `INVOICES_API`) |
| `url` | String | URL completa (ex: `https://api-dev.example.com`) |
| `testProjectId` | Long | FK para TestProject |
| `environmentId` | Long | FK para Environment |

### ApiCall (Catálogo de Endpoints)

Representa um endpoint catalogado e reutilizável.

| Campo | Tipo | Descrição |
| --- | --- | --- |
| `id` | Long | Identificador único |
| `projectId` | Long | FK para TestProject |
| `domainId` | Long | FK para Domain (opcional, atribuível depois) |
| `baseUrlId` | Long | FK para BaseUrl |
| `routeDefinition` | String | Path do endpoint |
| `method` | Enum | HttpMethod (GET, POST, PUT, DELETE, PATCH) |
| `description` | String | Descrição do que o endpoint faz |
| `requestExample` | Text | JSON de exemplo de request |
| `responseExample` | Text | JSON de exemplo de response |
| `customVariables` | Set | Variáveis customizadas utilizadas |
| `requiredParams` | Text | Parâmetros obrigatórios |

### AuthProfile (Perfil de Autenticação)

Representa um perfil de autenticação com credenciais encriptadas.

| Campo | Tipo | Descrição |
| --- | --- | --- |
| `id` | Long | Identificador único |
| `environmentId` | Long | FK para Environment |
| `name` | String | Nome do perfil |
| `credentials` | AuthCredentials | Credenciais (polimórfico) |

**Tipos de Credenciais:**
- `BearerTokenCredentials` - Token de serviço (encriptado)
- `BasicAuthCredentials` - Username/Password (encriptados)

---

## Regras de campo

A validação de campo deve ser executada primeiro, retornando `400 Bad Request` se qualquer uma for violada.

| Código | Campo | Regra | Racional | errorCode |
| --- | --- | --- | --- | --- |
| RC10.01.1 | `name` | Obrigatório, máx 255 chars | Identificação | `INVALID_FIELD_LENGTH` |
| RC10.01.2 | `inputType` | Obrigatório, deve ser MANUAL ou API_CALL | Discriminador | `REQUIRED_FIELD` |
| RC10.01.3 | `method` | Obrigatório se inputType=MANUAL | Consistência | `INVALID_HTTP_METHOD` |
| RC10.01.4 | `path` | Obrigatório se inputType=MANUAL, deve iniciar com `/` | Padrão REST | `INVALID_API_PATH` |
| RC10.01.5 | `apiCallId` | Obrigatório se inputType=API_CALL | Referência | `REQUIRED_FIELD` |
| RC10.01.6 | `authProfileId` | Obrigatório se `requiresAuth=true` | Segurança | `AUTH_PROFILE_REQUIRED` |
| RC10.01.7 | `testObjective` | Obrigatório, máx 2000 chars | Contexto para IA | `REQUIRED_FIELD` |

## Regras de negócio

As regras de negócio (RNs) geralmente resultam em `401 Unauthorized`, `403 Forbidden`, `409 Conflict` ou `422 Unprocessable Entity`

| Código | Regra | Racional | errorCode |
| --- | --- | --- | --- |
| RN10.01.1 | Usuário deve ser ADMIN/OWNER do projeto | Governança | `INSUFFICIENT_PERMISSIONS` (**TODO**) |
| RN10.01.2 | ApiCall deve existir (se inputType=API_CALL) | Referência válida | `API_CALL_NOT_FOUND` |
| RN10.01.3 | ApiCall deve pertencer ao TestProject | Consistência | `API_CALL_INVALID` |
| RN10.01.4 | Domain deve existir (se informado) | Consistência semântica | `DOMAIN_NOT_FOUND` |
| RN10.01.5 | AuthProfile deve pertencer ao Environment | Segurança | `AUTH_PROFILE_INVALID` |
| RN10.01.6 | Não pode existir outra Specification com mesmo `method + path + environment` | Evitar duplicidade | `SPEC_ALREADY_EXISTS` |
| RN10.01.7 | SupportingApiCalls devem pertencer ao TestProject | Consistência | `SUPPORTING_API_CALL_INVALID` |

## Respostas HTTP

| Categoria | Código HTTP | Descrição | Motivo de Retorno |
| --- | --- | --- | --- |
| Sucesso | **201 Created** | Specification criada com sucesso | Registro persistido |
| Erro Cliente | **400 Bad Request** | Payload inválido | Violação de RC10.01.x |
| Erro Cliente | **403 Forbidden** | Acesso negado | Usuário sem permissão no projeto |
| Erro Cliente | **404 Not Found** | Recurso não encontrado | TestProject, ApiCall, Environment não existe |
| Erro Cliente | **409 Conflict** | Conflito de estado | Specification duplicada |
| Erro Cliente | **422 Unprocessable Entity** | Regra de negócio violada | Violação de RN10.01.x |
| Erro Servidor | **500 Internal Server Error** | Erro inesperado | Falha interna não mapeada |

## Fluxo técnico

1. Valida permissões do usuário no Projeto (**TODO**)
2. Valida existência do `TestProject`
3. Valida `Environment` pertence ao `TestProject`
4. Se `inputType == API_CALL`:
   - Valida `ApiCall` existe e pertence ao `TestProject`
   - Extrai `method`, `path`, `domainId` do `ApiCall`
5. Se `inputType == MANUAL`:
   - Usa `method` e `path` do request
6. Valida `Domain` existe (se informado)
7. Valida `AuthProfile` (se `requiresAuth=true`)
8. Valida `supportingApiCallIds` pertencem ao `TestProject`
9. Verifica duplicidade (method + path + environment)
10. Persiste a Specification
11. Define status inicial:
    - `WAITING_APPROVAL` → se `approveBeforeGeneration = true`
    - `CREATED` → caso contrário
12. Publica evento `SPECIFICATION_CREATED` via RabbitMQ
13. Retorna SpecificationResponseDTO

---

## Comunicação Assíncrona

A comunicação entre **aegis-homolog-orchestrator** (Java) e **aegis-agents** (Python) é feita via **RabbitMQ**.

### Eventos

| Evento | Quando | Payload |
| --- | --- | --- |
| `SPECIFICATION_CREATED` | Specification criada | specId, projectId, envId, method, path, testObjective |
| `SPECIFICATION_STATUS_CHANGED` | Status alterado | specId, oldStatus, newStatus |

### Filas

- `aegis.specification.created` - Notifica agentes sobre nova spec
- `aegis.specification.status` - Atualizações de status dos agentes
