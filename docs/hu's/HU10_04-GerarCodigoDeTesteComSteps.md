> **COMO:** Sistema (IA geradora de código)
>
> **QUERO:** Gerar o código de teste (Karate DSL) de forma estruturada
>
> **PARA:** Criar testes auditáveis, legíveis e executáveis.

---

## Contexto

Após o planejamento ser aprovado, a IA inicia a **geração de código**. Cada Scenario aprovado é transformado em código Karate DSL, organizado em **STEPs semânticos**.

### Conceitos-Chave

| Conceito | Descrição |
|----------|-----------|
| **Feature** | Arquivo `.feature` com testes relacionados |
| **Scenario** | Caso de teste individual dentro da Feature |
| **STEP** | Microstep de código (comentário `#STEP{n}` + linhas subsequentes) |

### O que é um STEP?

Um STEP é uma **unidade de código auditável** dentro de um Scenario:

```gherkin
Scenario: Criar cliente com dados válidos
  # STEP1: Preparar dados do cliente
  * def cliente = { nome: 'João', cpf: '12345678900' }
  
  # STEP2: Enviar requisição POST
  Given url baseUrl + '/api/v1/clientes'
  And request cliente
  When method POST
  
  # STEP3: Validar resposta de sucesso
  Then status 201
  And match response.id == '#notnull'
  
  # STEP4: Validar estrutura da resposta
  And match response contains { nome: 'João' }
```

⚠️ **STEPs são para auditoria e diagnóstico, NÃO para progresso em tempo real.**
⚠️ **Progresso é emitido por Scenario, não por STEP.**

---

## Fluxo de Geração

```
ScenarioPlan (APPROVED)
        │
        ▼
┌───────────────────┐
│   Aegis Agent     │
│   (Python + LLM)  │
└───────────────────┘
        │
        │ Para cada Scenario:
        │ 1. Gerar código Karate
        │ 2. Organizar em STEPs
        │ 3. Persistir
        │ 4. Emitir evento
        │
        ▼
┌───────────────────┐
│   Feature         │
│   ├── Scenario 1  │
│   │   ├── STEP 1  │
│   │   ├── STEP 2  │
│   │   └── STEP 3  │
│   └── Scenario 2  │
│       └── ...     │
└───────────────────┘
        │
        ▼
  SCENARIO_COMPLETED (evento)
```

---

## Endpoint de Callback (Agente → Orchestrator)

<aside>
➡️

**POST** `/v1/internal/generation-jobs/{jobId}/scenarios/{scenarioId}/generated`

</aside>

Endpoint interno usado pelo Aegis Agent para enviar um Scenario gerado.

---

## Layouts

### Request (do Agente - Por Scenario)

| Campo | Tipo | Descrição | Exemplo | Req. |
| --- | --- | --- | --- | --- |
| `scenarioId` | UUID | ID do ScenarioPlan | `abc123-...` | Obrigatório |
| `featureCode` | String | Código Karate gerado | `Scenario: ...` | Obrigatório |
| `steps` | List\<StepDTO\> | STEPs extraídos | `[...]` | Obrigatório |
| `metadata` | Object | Metadados da geração | `{ ... }` | Opcional |

#### StepDTO

| Campo | Tipo | Descrição | Exemplo |
| --- | --- | --- | --- |
| `stepNumber` | Integer | Número do STEP | `1` |
| `description` | String | Descrição do STEP | `Preparar dados do cliente` |
| `code` | String | Código do STEP | `* def cliente = {...}` |
| `lineStart` | Integer | Linha inicial no arquivo | `5` |
| `lineEnd` | Integer | Linha final no arquivo | `8` |

#### Exemplo Request

```json
{
  "scenarioId": "abc123-def456-...",
  "featureCode": "Scenario: Criar cliente com dados válidos\n  # STEP1: Preparar dados do cliente\n  * def cliente = { nome: 'João', cpf: '12345678900' }\n  \n  # STEP2: Enviar requisição POST\n  Given url baseUrl + '/api/v1/clientes'\n  And request cliente\n  When method POST\n  \n  # STEP3: Validar resposta de sucesso\n  Then status 201\n  And match response.id == '#notnull'",
  "steps": [
    {
      "stepNumber": 1,
      "description": "Preparar dados do cliente",
      "code": "* def cliente = { nome: 'João', cpf: '12345678900' }",
      "lineStart": 2,
      "lineEnd": 3
    },
    {
      "stepNumber": 2,
      "description": "Enviar requisição POST",
      "code": "Given url baseUrl + '/api/v1/clientes'\nAnd request cliente\nWhen method POST",
      "lineStart": 5,
      "lineEnd": 8
    },
    {
      "stepNumber": 3,
      "description": "Validar resposta de sucesso",
      "code": "Then status 201\nAnd match response.id == '#notnull'",
      "lineStart": 10,
      "lineEnd": 12
    }
  ],
  "metadata": {
    "tokensUsed": 1250,
    "generationTimeMs": 3500,
    "modelVersion": "gpt-4-turbo"
  }
}
```

### Response

```json
{
  "success": true,
  "scenarioId": "abc123-def456-...",
  "status": "GENERATED",
  "stepsCount": 3
}
```

---

## Evento de Progresso

Ao persistir cada Scenario, o sistema emite um evento de progresso:

```json
{
  "type": "SCENARIO_COMPLETED",
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "scenarioId": "abc123-def456-...",
  "featureId": "feature-uuid-...",
  "title": "Criar cliente com dados válidos",
  "stepsCount": 3,
  "progress": {
    "completedScenarios": 5,
    "totalScenarios": 12,
    "percentage": 41.67
  },
  "timestamp": "2026-01-28T10:15:30Z"
}
```

---

## Endpoint de Callback (Feature Completa)

<aside>
➡️

**POST** `/v1/internal/generation-jobs/{jobId}/features/{featureId}/completed`

</aside>

Notifica que todos os Scenarios de uma Feature foram gerados.

### Request

```json
{
  "featureId": "feature-uuid-...",
  "featureFile": "Feature: Autenticação de Usuários\n\nBackground:\n  * url baseUrl\n\nScenario: Login com credenciais válidas\n  ...",
  "scenariosGenerated": 3,
  "totalSteps": 12,
  "filePath": "src/test/resources/features/auth/login.feature"
}
```

---

## Endpoint de Conclusão do Job

<aside>
➡️

**POST** `/v1/internal/generation-jobs/{jobId}/completed`

</aside>

Notifica que toda a geração foi concluída.

### Request

```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "summary": {
    "featuresGenerated": 3,
    "scenariosGenerated": 12,
    "totalSteps": 48,
    "generationTimeMs": 45000
  },
  "status": "SUCCESS"
}
```

---

## Entidades Relacionadas

### Feature (Entidade de Produção)

| Campo | Tipo | Descrição |
| --- | --- | --- |
| `id` | UUID | Identificador único |
| `testProjectId` | Long | FK para TestProject |
| `jobId` | UUID | FK para GenerationJob (nullable - pode ser criada manualmente) |
| `name` | String | Nome da Feature |
| `description` | Text | Descrição |
| `filePath` | String | Caminho do arquivo `.feature` |
| `featureCode` | Text | Código Karate completo |
| `status` | Enum | `DRAFT`, `GENERATED`, `VALIDATED`, `FAILED` |
| `createdAt` | Timestamp | Data de criação |
| `updatedAt` | Timestamp | Última atualização |

### Scenario (Entidade de Produção)

| Campo | Tipo | Descrição |
| --- | --- | --- |
| `id` | UUID | Identificador único |
| `featureId` | UUID | FK para Feature |
| `scenarioPlanId` | UUID | FK para ScenarioPlan (nullable) |
| `title` | String | Título do Scenario |
| `description` | Text | Descrição |
| `type` | Enum | Tipo do teste |
| `priority` | Enum | Prioridade |
| `scenarioCode` | Text | Código Karate do Scenario |
| `lineStart` | Integer | Linha inicial no arquivo |
| `lineEnd` | Integer | Linha final no arquivo |
| `status` | Enum | `DRAFT`, `GENERATED`, `PASSED`, `FAILED` |
| `lastExecutionAt` | Timestamp | Última execução |
| `executionCount` | Integer | Total de execuções |
| `passRate` | Decimal | Taxa de sucesso |
| `createdAt` | Timestamp | Data de criação |

### Step (Entidade de Produção)

| Campo | Tipo | Descrição |
| --- | --- | --- |
| `id` | UUID | Identificador único |
| `scenarioId` | UUID | FK para Scenario |
| `stepNumber` | Integer | Número sequencial do STEP |
| `description` | String | Descrição semântica |
| `code` | Text | Código do STEP |
| `lineStart` | Integer | Linha inicial |
| `lineEnd` | Integer | Linha final |
| `status` | Enum | `ACTIVE`, `DEPRECATED` |
| `createdAt` | Timestamp | Data de criação |

---

## Desacoplamento de Framework

**(DÚVIDA)** Para suportar outros frameworks no futuro, devemos:
1. Criar uma abstração `TestFrameworkGenerator`?
2. Armazenar o código de forma agnóstica?
3. Usar um formato intermediário antes de gerar Karate?

Atualmente focamos em **Karate DSL**, mas a estrutura Feature/Scenario/Step é agnóstica.

---

## Regras de Negócio

| Código | Regra | Racional | errorCode |
| --- | --- | --- | --- |
| RN10.04.1 | Job deve estar em status GENERATING | Fluxo | `JOB_INVALID_STATUS` |
| RN10.04.2 | ScenarioPlan deve estar APPROVED | Consistência | `SCENARIO_NOT_APPROVED` |
| RN10.04.3 | Cada STEP deve ter número sequencial único | Integridade | `STEP_NUMBER_INVALID` |
| RN10.04.4 | Código gerado deve ser válido sintaticamente | Qualidade | `INVALID_KARATE_SYNTAX` |

---

## Comunicação Assíncrona

### Eventos Recebidos (do Agente)

| Evento | Quando | Payload |
| --- | --- | --- |
| `GENERATION_STARTED` | Geração iniciou | jobId |
| `SCENARIO_GENERATED` | Um Scenario foi gerado | jobId, scenarioId, code |
| `FEATURE_COMPLETED` | Todos Scenarios da Feature gerados | jobId, featureId |
| `GENERATION_COMPLETED` | Toda geração concluída | jobId, summary |
| `GENERATION_FAILED` | Falha na geração | jobId, error |

### Eventos Publicados (para Frontend/WebSocket)

| Evento | Quando | Payload |
| --- | --- | --- |
| `SCENARIO_COMPLETED` | Scenario persistido | scenarioId, title, progress |
| `FEATURE_COMPLETED` | Feature finalizada | featureId, name, scenariosCount |
| `JOB_COMPLETED` | Job finalizado | jobId, summary |

---

## Estrutura de Arquivos Gerados

```
src/test/resources/
└── features/
    └── {domain}/
        ├── {feature-name}.feature
        └── ...

Exemplo:
src/test/resources/
└── features/
    ├── auth/
    │   └── login.feature
    └── customers/
        ├── create-customer.feature
        └── list-customers.feature
```

---

## Resultado Esperado

- ✅ Features criadas e persistidas
- ✅ Scenarios criados com código Karate
- ✅ STEPs extraídos e persistidos para auditoria
- ✅ Eventos de progresso emitidos por Scenario
- ✅ Arquivos `.feature` gerados
- ✅ Job atualizado para status `VALIDATING` ou `COMPLETED`
