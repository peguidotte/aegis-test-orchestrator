> **COMO:** Sistema (Aegis Tests + Aegis Agent)
>
> **QUERO:** Transformar insumos do projeto em um plano estruturado de testes
>
> **PARA:** Definir claramente o que será gerado antes de escrever código.

---

## Contexto

O **Planejamento** é a fase onde a IA analisa todo o contexto capturado e produz um **ScenarioPlan** — um plano detalhado de Features e Scenarios que serão gerados.

Esta fase é crucial porque:
- Define a **estrutura** dos testes antes da geração
- Permite **revisão humana** antes de gastar recursos com geração
- Garante **cobertura adequada** do escopo definido
- Facilita **estimativas** de tempo e custo

### O que é um ScenarioPlan?

Um ScenarioPlan é a representação estruturada do que a IA pretende gerar:

```
ScenarioPlan
├── Feature: Autenticação de Usuários
│   ├── Scenario: Login com credenciais válidas
│   ├── Scenario: Login com senha incorreta
│   └── Scenario: Login com usuário inexistente
│
└── Feature: Gestão de Clientes
    ├── Scenario: Criar cliente com dados válidos
    ├── Scenario: Criar cliente com CPF duplicado
    └── Scenario: Listar clientes com paginação
```

⚠️ **Importante:** Nenhum STEP é criado nesta fase. Nenhum código é gerado ainda.

---

## Fluxo do Planejamento

```
GenerationJob (PLANNING)
        │
        ▼
┌───────────────────┐
│   Aegis Agent     │
│   (Python + LLM)  │
└───────────────────┘
        │
        │ Analisa:
        │ - Specification/ApiDocs/Repo
        │ - Contexto do projeto
        │ - Padrões do Aegis
        │
        ▼
┌───────────────────┐
│   ScenarioPlan    │
│   (Features +     │
│    Scenarios)     │
└───────────────────┘
        │
        ▼
   Persistir no BD
        │
        ▼
  Status: PLANNED
```

---

## Tópico de Callback (Agente → Orchestrator)

<aside>
➡️

**Tópico** `aegis-test.test-generation.planned`

</aside>

---

## Layouts

### Request (do Agente)

| Campo | Tipo | Descrição | Exemplo | Req. |
| --- | --- | --- | --- | --- |
| `jobId` | UUID | ID do GenerationJob | `550e8400-...` | Obrigatório |
| `features` | List\<FeaturePlan\> | Lista de Features planejadas | `[...]` | Obrigatório |
| `summary` | String | Resumo do planejamento | `3 features, 12 scenarios` | Obrigatório |
| `estimatedDuration` | Duration | Estimativa de tempo de geração | `PT15M` | Opcional |
| `coverageAnalysis` | Object | Análise de cobertura | `{ ... }` | Opcional |

#### FeaturePlan

| Campo | Tipo | Descrição | Exemplo |
| --- | --- | --- | --- |
| `name` | String | Nome da Feature | `Autenticação de Usuários` |
| `description` | String | Descrição | `Testes do módulo de auth` |
| `scenarios` | List\<ScenarioPlan\> | Scenarios planejados | `[...]` |

#### ScenarioPlan

| Campo | Tipo | Descrição | Exemplo |
| --- | --- | --- | --- |
| `title` | String | Título do Scenario | `Login com credenciais válidas` |
| `description` | String | Descrição da intenção | `Validar que...` |
| `type` | Enum | Tipo do teste | `POSITIVE`, `NEGATIVE`, `EDGE_CASE` |
| `priority` | Enum | Prioridade | `HIGH`, `MEDIUM`, `LOW` |
| `tags` | List\<String\> | Tags sugeridas | `["auth", "smoke"]` |
| `estimatedSteps` | Integer | Estimativa de STEPs | `5` |

#### Exemplo Request

```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "features": [
    {
      "name": "Autenticação de Usuários",
      "description": "Testes completos do módulo de autenticação",
      "scenarios": [
        {
          "title": "Login com credenciais válidas",
          "description": "Validar que um usuário com credenciais corretas consegue autenticar",
          "type": "POSITIVE",
          "priority": "HIGH",
          "tags": ["auth", "smoke", "critical"],
          "estimatedSteps": 4
        },
        {
          "title": "Login com senha incorreta",
          "description": "Validar que o sistema rejeita senha incorreta com mensagem apropriada",
          "type": "NEGATIVE",
          "priority": "HIGH",
          "tags": ["auth", "security"],
          "estimatedSteps": 3
        },
        {
          "title": "Login com usuário inexistente",
          "description": "Validar comportamento ao tentar login com email não cadastrado",
          "type": "NEGATIVE",
          "priority": "MEDIUM",
          "tags": ["auth"],
          "estimatedSteps": 3
        }
      ]
    },
    {
      "name": "Gestão de Clientes",
      "description": "Testes CRUD do módulo de clientes",
      "scenarios": [
        {
          "title": "Criar cliente com dados válidos",
          "description": "Validar criação de cliente com todos os campos obrigatórios",
          "type": "POSITIVE",
          "priority": "HIGH",
          "tags": ["customers", "crud"],
          "estimatedSteps": 6
        }
      ]
    }
  ],
  "summary": "2 features, 4 scenarios, estimativa de ~15 minutos para geração",
  "estimatedDuration": "PT15M",
  "coverageAnalysis": {
    "endpointsCovered": 3,
    "totalEndpoints": 5,
    "coveragePercentage": 60,
    "missingEndpoints": ["/api/v1/users/{id}", "/api/v1/users/{id}/avatar"]
  }
}
```

### Response

```json
{
  "success": true,
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PLANNED",
  "featuresCount": 2,
  "scenariosCount": 4
}
```

---

## Endpoint para Visualizar o Plano

<aside>
➡️

**GET** `/v1/generation-jobs/{jobId}/plan`

</aside>

Retorna o plano gerado para visualização do usuário.

### Response

```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PLANNED",
  "plan": {
    "features": [...],
    "summary": "2 features, 4 scenarios",
    "estimatedDuration": "PT15M",
    "coverageAnalysis": {...}
  },
  "createdAt": "2026-01-28T10:05:00Z",
  "requiresApproval": true
}
```

---

## Endpoint para Aprovar/Rejeitar o Plano

<aside>
➡️

**POST** `/v1/generation-jobs/{jobId}/plan/review`

</aside>

Permite que o usuário aprove, edite ou rejeite o plano.

### Request

| Campo | Tipo | Descrição | Exemplo | Req. |
| --- | --- | --- | --- | --- |
| `decision` | Enum | Decisão do usuário | `APPROVE`, `APPROVE_WITH_EDITS`, `REJECT` | Obrigatório |
| `edits` | Object | Edições aplicadas | `{ ... }` | Condicional* |
| `rejectionReason` | String | Motivo da rejeição | `Falta cobertura de...` | Condicional** |

> \* Obrigatório se `decision == APPROVE_WITH_EDITS`
>
> \*\* Obrigatório se `decision == REJECT`

#### Exemplo: Aprovar

```json
{
  "decision": "APPROVE"
}
```

#### Exemplo: Aprovar com Edições

```json
{
  "decision": "APPROVE_WITH_EDITS",
  "edits": {
    "removedScenarios": ["scenario-uuid-1"],
    "addedScenarios": [
      {
        "featureName": "Autenticação de Usuários",
        "title": "Login com token expirado",
        "description": "Validar comportamento com token JWT expirado",
        "type": "NEGATIVE",
        "priority": "HIGH"
      }
    ],
    "modifiedScenarios": [
      {
        "scenarioId": "scenario-uuid-2",
        "newTitle": "Login com credenciais válidas - fluxo completo"
      }
    ]
  }
}
```

#### Exemplo: Rejeitar

```json
{
  "decision": "REJECT",
  "rejectionReason": "Falta cobertura dos endpoints de pagamento. Precisa incluir testes de PIX e boleto."
}
```

### Response

```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "APPROVED",
  "nextStep": "GENERATING",
  "message": "Plan approved. Test generation will start shortly."
}
```

---

## ScenarioType (Enum)

| Valor | Descrição |
| --- | --- |
| `POSITIVE` | Testa o caminho feliz |
| `NEGATIVE` | Testa casos de erro esperados |
| `EDGE_CASE` | Testa limites e casos extremos |
| `SECURITY` | Testes de segurança |
| `PERFORMANCE` | Testes de performance/carga |

---

## Entidades Relacionadas

### FeaturePlan (NOVA)

| Campo | Tipo | Descrição |
| --- | --- | --- |
| `id` | UUID | Identificador único |
| `jobId` | UUID | FK para GenerationJob |
| `name` | String | Nome da Feature |
| `description` | Text | Descrição |
| `order` | Integer | Ordem de exibição |
| `status` | Enum | Status do planejamento |
| `createdAt` | Timestamp | Data de criação |

### ScenarioPlan (NOVA)

| Campo | Tipo | Descrição |
| --- | --- | --- |
| `id` | UUID | Identificador único |
| `featurePlanId` | UUID | FK para FeaturePlan |
| `title` | String | Título do Scenario |
| `description` | Text | Descrição da intenção |
| `type` | Enum | Tipo do teste |
| `priority` | Enum | Prioridade |
| `estimatedSteps` | Integer | Estimativa de STEPs |
| `status` | Enum | `PLANNED`, `APPROVED`, `REMOVED`, `MODIFIED` |
| `order` | Integer | Ordem de exibição |

---

## Regras de Negócio

| Código | Regra | Racional | errorCode |
| --- | --- | --- | --- |
| RN10.03.1 | Job deve estar em status PLANNING para receber plano | Fluxo | `JOB_INVALID_STATUS` |
| RN10.03.2 | Plano deve ter pelo menos 1 Feature | Mínimo viável | `PLAN_EMPTY` |
| RN10.03.3 | Cada Feature deve ter pelo menos 1 Scenario | Consistência | `FEATURE_EMPTY` |
| RN10.03.4 | Apenas Jobs em status PLANNED ou WAITING_APPROVAL podem ser revisados | Fluxo | `JOB_NOT_REVIEWABLE` |
| RN10.03.5 | Usuário deve ter permissão no TestProject | Segurança | `INSUFFICIENT_PERMISSIONS` |

---

## Comunicação Assíncrona

### Eventos Recebidos (do Agente)

| Evento | Quando | Payload |
| --- | --- | --- |
| `PLANNING_STARTED` | IA iniciou planejamento | jobId |
| `PLANNING_PROGRESS` | Progresso do planejamento | jobId, percentage, message |
| `PLANNING_COMPLETED` | Planejamento concluído | jobId, plan |
| `PLANNING_FAILED` | Falha no planejamento | jobId, error |

### Eventos Publicados

| Evento | Quando | Payload |
| --- | --- | --- |
| `PLAN_READY_FOR_REVIEW` | Plano pronto para revisão | jobId, summary |
| `PLAN_APPROVED` | Plano aprovado | jobId |
| `PLAN_REJECTED` | Plano rejeitado | jobId, reason |

---

## Fluxo de Aprovação

```
PLANNED ────────────────────────────────────────────────────┐
    │                                                       │
    │  requiresApproval = true                              │  requiresApproval = false
    │                                                       │
    ▼                                                       │
WAITING_APPROVAL                                            │
    │                                                       │
    ├──► APPROVE ──────────► APPROVED ──────────────────────┤
    │                                                       │
    ├──► APPROVE_WITH_EDITS ──► APPROVED_WITH_EDITS ────────┤
    │                                                       │
    └──► REJECT ──────────► REJECTED ──► PLANNING (retry)   │
                                                            │
                                                            ▼
                                                       GENERATING
```

**(DÚVIDA)** O `requiresApproval` deve vir da Specification ou do GenerationJob?

---

## Resultado Esperado

- ✅ ScenarioPlan criado e persistido
- ✅ Features e Scenarios planejados
- ✅ Cobertura analisada
- ✅ Plano disponível para revisão (se necessário)
- ❌ Nenhum STEP criado
- ❌ Nenhum código gerado
- ❌ Nenhuma Feature/Scenario final criada (apenas planos)
