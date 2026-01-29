> **COMO:** Sistema (Aegis Tests)
>
> **QUERO:** Executar automaticamente os testes gerados e diagnosticar falhas
>
> **PARA:** Validar qualidade e detectar falhas reais.

---

## Contexto

Após a geração dos testes, o sistema executa automaticamente para validar:
- **Qualidade do código gerado** (sintaxe, estrutura)
- **Funcionamento do teste** (executa corretamente?)
- **Comportamento da aplicação** (API responde como esperado?)

A IA também analisa as falhas e classifica a **causa raiz**, ajudando o usuário a entender se o problema é:
- No teste gerado
- Na aplicação testada
- No ambiente de execução

---

## Tipos de Falha

| Tipo | Descrição | Exemplo | Ação Sugerida |
|------|-----------|---------|---------------|
| `TEST_ERROR` | Erro no código do teste (IA) | Sintaxe Karate inválida | Regenerar teste |
| `APPLICATION_ERROR` | Erro na aplicação testada | API retornou 500 | Reportar ao time de dev |
| `ENVIRONMENT_ERROR` | Erro de ambiente | Timeout, conexão recusada | Verificar infra |
| `ASSERTION_FAILURE` | Asserção falhou | Esperava 200, recebeu 400 | Analisar contrato |
| `DATA_ERROR` | Problema com dados de teste | CPF duplicado no banco | Limpar dados |

---

## Fluxo de Validação

```
Testes Gerados
      │
      ▼
┌─────────────────┐
│   Executar      │
│   Testes        │
│   (Karate)      │
└─────────────────┘
      │
      ▼
┌─────────────────┐
│   Coletar       │
│   Resultados    │
└─────────────────┘
      │
      ├──► PASSED ──► Marcar Scenario como PASSED
      │
      └──► FAILED ──┐
                    │
                    ▼
            ┌─────────────────┐
            │   IA Diagnóstico│
            │   (Classificar  │
            │    causa raiz)  │
            └─────────────────┘
                    │
                    ▼
            ┌─────────────────┐
            │   Persistir     │
            │   StepAnalysis  │
            └─────────────────┘
```

---

## Endpoint de Callback (Resultado de Execução)

<aside>
➡️

**POST** `/v1/internal/generation-jobs/{jobId}/validation-results`

</aside>

Endpoint interno para receber resultados da execução de testes.

---

## Layouts

### Request (Resultado da Execução)

| Campo | Tipo | Descrição | Exemplo | Req. |
| --- | --- | --- | --- | --- |
| `jobId` | UUID | ID do GenerationJob | `550e8400-...` | Obrigatório |
| `executionId` | UUID | ID único desta execução | `exec-123-...` | Obrigatório |
| `results` | List\<ScenarioResult\> | Resultados por Scenario | `[...]` | Obrigatório |
| `summary` | ExecutionSummary | Resumo geral | `{ ... }` | Obrigatório |
| `executedAt` | Timestamp | Quando executou | `2026-01-28T10:30:00Z` | Obrigatório |
| `duration` | Duration | Tempo total | `PT2M30S` | Obrigatório |

#### ScenarioResult

| Campo | Tipo | Descrição | Exemplo |
| --- | --- | --- | --- |
| `scenarioId` | UUID | ID do Scenario | `abc123-...` |
| `status` | Enum | `PASSED`, `FAILED`, `SKIPPED` | `FAILED` |
| `duration` | Duration | Tempo de execução | `PT5S` |
| `failedAtStep` | Integer | Número do STEP que falhou | `2` |
| `errorMessage` | String | Mensagem de erro | `status code was: 401` |
| `stackTrace` | String | Stack trace (se houver) | `...` |

#### ExecutionSummary

| Campo | Tipo | Descrição |
| --- | --- | --- |
| `totalScenarios` | Integer | Total de scenarios |
| `passed` | Integer | Quantos passaram |
| `failed` | Integer | Quantos falharam |
| `skipped` | Integer | Quantos foram pulados |
| `passRate` | Decimal | Taxa de sucesso |

#### Exemplo Request

```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "executionId": "exec-789-...",
  "results": [
    {
      "scenarioId": "abc123-...",
      "status": "PASSED",
      "duration": "PT3S",
      "failedAtStep": null,
      "errorMessage": null
    },
    {
      "scenarioId": "def456-...",
      "status": "FAILED",
      "duration": "PT5S",
      "failedAtStep": 2,
      "errorMessage": "status code was: 401, expected: 200",
      "stackTrace": "com.intuit.karate.AssertionError: ..."
    }
  ],
  "summary": {
    "totalScenarios": 12,
    "passed": 11,
    "failed": 1,
    "skipped": 0,
    "passRate": 91.67
  },
  "executedAt": "2026-01-28T10:30:00Z",
  "duration": "PT2M30S"
}
```

---

## Endpoint de Callback (Diagnóstico da IA)

<aside>
➡️

**POST** `/v1/internal/generation-jobs/{jobId}/diagnostics`

</aside>

Endpoint para receber análise da IA sobre as falhas.

### Request (Diagnóstico)

| Campo | Tipo | Descrição | Exemplo | Req. |
| --- | --- | --- | --- | --- |
| `jobId` | UUID | ID do GenerationJob | `550e8400-...` | Obrigatório |
| `analyses` | List\<StepAnalysis\> | Análises por STEP | `[...]` | Obrigatório |

#### StepAnalysis

| Campo | Tipo | Descrição | Exemplo |
| --- | --- | --- | --- |
| `scenarioId` | UUID | ID do Scenario | `abc123-...` |
| `stepNumber` | Integer | Número do STEP | `2` |
| `failureType` | Enum | Classificação da falha | `APPLICATION_ERROR` |
| `diagnosis` | String | Análise da IA | `O endpoint retornou 401...` |
| `rootCause` | String | Causa raiz identificada | `Token JWT expirado` |
| `suggestedAction` | String | Ação sugerida | `Renovar token de autenticação` |
| `confidence` | Decimal | Confiança da análise (0-1) | `0.85` |
| `relatedCode` | String | Código do STEP | `Given url '...'` |

#### Exemplo Request

```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "analyses": [
    {
      "scenarioId": "def456-...",
      "stepNumber": 2,
      "failureType": "APPLICATION_ERROR",
      "diagnosis": "O endpoint /api/v1/clientes retornou HTTP 401 Unauthorized. O teste estava tentando criar um cliente mas a requisição foi rejeitada por falta de autenticação.",
      "rootCause": "O AuthProfile configurado possui um token JWT que expirou ou é inválido para este ambiente.",
      "suggestedAction": "Verifique se o AuthProfile 'prod-api-token' está com credenciais válidas para o ambiente de teste.",
      "confidence": 0.92,
      "relatedCode": "Given url baseUrl + '/api/v1/clientes'\nAnd request cliente\nWhen method POST"
    }
  ]
}
```

---

## Endpoint para Consultar Diagnósticos

<aside>
➡️

**GET** `/v1/generation-jobs/{jobId}/diagnostics`

</aside>

Retorna todos os diagnósticos do Job.

### Response

```json
{
  "jobId": "550e8400-...",
  "executionId": "exec-789-...",
  "summary": {
    "totalFailures": 1,
    "byType": {
      "APPLICATION_ERROR": 1,
      "TEST_ERROR": 0,
      "ENVIRONMENT_ERROR": 0
    }
  },
  "diagnostics": [
    {
      "scenarioId": "def456-...",
      "scenarioTitle": "Criar cliente com dados válidos",
      "featureName": "Gestão de Clientes",
      "stepNumber": 2,
      "stepDescription": "Enviar requisição POST",
      "failureType": "APPLICATION_ERROR",
      "diagnosis": "O endpoint retornou 401...",
      "rootCause": "Token JWT expirado",
      "suggestedAction": "Renovar token de autenticação",
      "confidence": 0.92
    }
  ]
}
```

---

## Endpoint para Consultar Diagnóstico de um STEP

<aside>
➡️

**GET** `/v1/scenarios/{scenarioId}/steps/{stepNumber}/analysis`

</aside>

Retorna análise detalhada de um STEP específico.

### Response

```json
{
  "scenarioId": "def456-...",
  "stepNumber": 2,
  "step": {
    "description": "Enviar requisição POST",
    "code": "Given url baseUrl + '/api/v1/clientes'\nAnd request cliente\nWhen method POST",
    "lineStart": 5,
    "lineEnd": 8
  },
  "analysis": {
    "failureType": "APPLICATION_ERROR",
    "diagnosis": "O endpoint retornou 401 Unauthorized...",
    "rootCause": "Token JWT expirado",
    "suggestedAction": "Renovar token de autenticação",
    "confidence": 0.92
  },
  "execution": {
    "errorMessage": "status code was: 401, expected: 200",
    "executedAt": "2026-01-28T10:30:05Z",
    "duration": "PT2S"
  },
  "history": [
    {
      "executionId": "exec-789-...",
      "status": "FAILED",
      "executedAt": "2026-01-28T10:30:05Z"
    }
  ]
}
```

---

## Entidades Relacionadas

### TestExecution (NOVA)

| Campo | Tipo | Descrição |
| --- | --- | --- |
| `id` | UUID | Identificador único |
| `jobId` | UUID | FK para GenerationJob |
| `status` | Enum | `RUNNING`, `COMPLETED`, `FAILED` |
| `totalScenarios` | Integer | Total de scenarios |
| `passed` | Integer | Quantos passaram |
| `failed` | Integer | Quantos falharam |
| `skipped` | Integer | Quantos foram pulados |
| `passRate` | Decimal | Taxa de sucesso |
| `duration` | Duration | Tempo total |
| `executedAt` | Timestamp | Quando executou |
| `executedBy` | String | Quem/O que disparou |

### ScenarioExecution (NOVA)

| Campo | Tipo | Descrição |
| --- | --- | --- |
| `id` | UUID | Identificador único |
| `executionId` | UUID | FK para TestExecution |
| `scenarioId` | UUID | FK para Scenario |
| `status` | Enum | `PASSED`, `FAILED`, `SKIPPED` |
| `duration` | Duration | Tempo de execução |
| `failedAtStep` | Integer | STEP que falhou (nullable) |
| `errorMessage` | Text | Mensagem de erro |
| `stackTrace` | Text | Stack trace |

### StepAnalysis (NOVA)

| Campo | Tipo | Descrição |
| --- | --- | --- |
| `id` | UUID | Identificador único |
| `scenarioExecutionId` | UUID | FK para ScenarioExecution |
| `stepNumber` | Integer | Número do STEP |
| `failureType` | Enum | Classificação da falha |
| `diagnosis` | Text | Análise da IA |
| `rootCause` | Text | Causa raiz identificada |
| `suggestedAction` | Text | Ação sugerida |
| `confidence` | Decimal | Confiança (0-1) |
| `createdAt` | Timestamp | Data de criação |

---

## Ações Baseadas em Diagnóstico

| failureType | Ação Automática | Ação Manual Sugerida |
|-------------|-----------------|---------------------|
| `TEST_ERROR` | Marcar para regeneração | Revisar código gerado |
| `APPLICATION_ERROR` | Criar issue (integração) | Notificar time de dev |
| `ENVIRONMENT_ERROR` | Retry automático (1x) | Verificar infra |
| `ASSERTION_FAILURE` | - | Revisar contrato esperado |
| `DATA_ERROR` | - | Limpar/Resetar dados |

**(DÚVIDA)** Devemos implementar retry automático para erros de ambiente?

**(DÚVIDA)** Devemos integrar com sistemas de issue tracking (Jira, GitHub Issues)?

---

## Regras de Negócio

| Código | Regra | Racional | errorCode |
| --- | --- | --- | --- |
| RN10.06.1 | Job deve estar em status VALIDATING ou COMPLETED | Fluxo | `JOB_INVALID_STATUS` |
| RN10.06.2 | Cada execução deve ter ID único | Rastreabilidade | `DUPLICATE_EXECUTION_ID` |
| RN10.06.3 | Diagnóstico só pode ser criado para Scenarios FAILED | Consistência | `SCENARIO_NOT_FAILED` |
| RN10.06.4 | Confidence deve estar entre 0 e 1 | Validação | `INVALID_CONFIDENCE` |

---

## Comunicação Assíncrona

### Eventos Recebidos

| Evento | Quando | Payload |
| --- | --- | --- |
| `VALIDATION_STARTED` | Execução iniciou | jobId, executionId |
| `SCENARIO_EXECUTED` | Um Scenario foi executado | scenarioId, status |
| `VALIDATION_COMPLETED` | Execução terminou | jobId, summary |
| `DIAGNOSIS_COMPLETED` | IA terminou análise | jobId, analyses |

### Eventos Publicados

| Evento | Quando | Payload |
| --- | --- | --- |
| `EXECUTION_RESULT_READY` | Resultado pronto | jobId, summary |
| `DIAGNOSIS_READY` | Diagnóstico pronto | jobId, failureCount |

---

## Visualização de Resultados (UI Sugerida)

### Resumo de Execução

```
┌─────────────────────────────────────────────────────────┐
│  Resultado da Execução                     ⏱️ 2m 30s    │
│                                                         │
│  ✅ 11 Passed   ❌ 1 Failed   ⏭️ 0 Skipped              │
│                                                         │
│  Taxa de Sucesso: 91.67%                                │
└─────────────────────────────────────────────────────────┘
```

### Detalhes de Falha com Diagnóstico

```
┌─────────────────────────────────────────────────────────┐
│  ❌ Criar cliente com dados válidos                     │
│     Feature: Gestão de Clientes                         │
│                                                         │
│  STEP 2: Enviar requisição POST                         │
│  ┌───────────────────────────────────────────────────┐  │
│  │ Given url baseUrl + '/api/v1/clientes'            │  │
│  │ And request cliente                               │  │
│  │ When method POST                                  │  │
│  └───────────────────────────────────────────────────┘  │
│                                                         │
│  🔍 Diagnóstico (92% confiança)                         │
│  Tipo: APPLICATION_ERROR                                │
│                                                         │
│  O endpoint retornou 401 Unauthorized. O teste estava   │
│  tentando criar um cliente mas a requisição foi         │
│  rejeitada por falta de autenticação.                   │
│                                                         │
│  💡 Causa Raiz: Token JWT expirado                      │
│                                                         │
│  🔧 Ação Sugerida: Verifique se o AuthProfile           │
│     'prod-api-token' está com credenciais válidas.      │
└─────────────────────────────────────────────────────────┘
```

---

## Resultado Esperado

- ✅ Testes executados automaticamente após geração
- ✅ Resultados persistidos por Scenario e por STEP
- ✅ Falhas classificadas por tipo (TEST, APPLICATION, ENVIRONMENT)
- ✅ Diagnóstico da IA com causa raiz e ação sugerida
- ✅ StepAnalysis atrelada ao STEP específico que falhou
- ✅ Histórico de execuções mantido
- ✅ Visualização clara para o usuário tomar decisões
