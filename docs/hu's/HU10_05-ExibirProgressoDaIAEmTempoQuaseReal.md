> **COMO:** Usuário do Aegis Tests
>
> **QUERO:** Visualizar o progresso da geração de testes em tempo quase real
>
> **PARA:** Acompanhar o que a IA está construindo sem tela de loading infinito.

---

## Contexto

Durante a geração de testes, o usuário precisa de **feedback visual** sobre o progresso. Esta HU define como o progresso é comunicado e exibido.

### Princípios de UX

| Princípio | Descrição |
|-----------|-----------|
| **Progresso por Scenario** | Unidade de progresso visível ao usuário |
| **STEPs não são streamados** | Muito granular, geraria ruído |
| **Feedback incremental** | Cada Scenario concluído atualiza a UI |
| **Sem loading infinito** | Sempre há indicação de progresso |

⚠️ **STEP não é unidade de progresso em tempo real**
⚠️ **Progresso é emitido por Scenario**

---

## Mecanismos de Comunicação

### Opção 1: WebSocket (Recomendado)

Conexão bidirecional para atualizações em tempo real.

```
Cliente ◄──────────────► Servidor
         WebSocket
         
Eventos:
- SCENARIO_COMPLETED
- FEATURE_COMPLETED
- JOB_STATUS_CHANGED
- JOB_COMPLETED
- JOB_FAILED
```

### Opção 2: Polling Inteligente (MVP)

Requisições periódicas com backoff exponencial.

```
Cliente ───► GET /jobs/{id}/progress ───► Servidor
        ◄─── { progress, events } ◄───

Intervalo:
- Início: 2s
- Durante geração: 3s
- Idle: 10s
```

**(DÚVIDA)** Qual mecanismo priorizar no MVP?

---

## Endpoint WebSocket

<aside>
➡️

**WS** `/v1/generation-jobs/{jobId}/progress`

</aside>

Estabelece conexão WebSocket para receber atualizações em tempo real.

### Handshake

```
GET /v1/generation-jobs/{jobId}/progress
Upgrade: websocket
Connection: Upgrade
Authorization: Bearer {token}
```

### Mensagens do Servidor

#### SCENARIO_COMPLETED

```json
{
  "type": "SCENARIO_COMPLETED",
  "jobId": "550e8400-...",
  "data": {
    "scenarioId": "abc123-...",
    "featureId": "feature-uuid-...",
    "featureName": "Autenticação de Usuários",
    "title": "Login com credenciais válidas",
    "stepsCount": 4
  },
  "progress": {
    "completedScenarios": 5,
    "totalScenarios": 12,
    "completedFeatures": 1,
    "totalFeatures": 3,
    "percentage": 41.67
  },
  "timestamp": "2026-01-28T10:15:30Z"
}
```

#### FEATURE_COMPLETED

```json
{
  "type": "FEATURE_COMPLETED",
  "jobId": "550e8400-...",
  "data": {
    "featureId": "feature-uuid-...",
    "name": "Autenticação de Usuários",
    "scenariosCount": 4,
    "totalSteps": 16
  },
  "progress": {
    "completedScenarios": 4,
    "totalScenarios": 12,
    "completedFeatures": 1,
    "totalFeatures": 3,
    "percentage": 33.33
  },
  "timestamp": "2026-01-28T10:16:00Z"
}
```

#### JOB_STATUS_CHANGED

```json
{
  "type": "JOB_STATUS_CHANGED",
  "jobId": "550e8400-...",
  "data": {
    "previousStatus": "GENERATING",
    "currentStatus": "VALIDATING",
    "message": "All scenarios generated. Starting validation..."
  },
  "timestamp": "2026-01-28T10:20:00Z"
}
```

#### JOB_COMPLETED

```json
{
  "type": "JOB_COMPLETED",
  "jobId": "550e8400-...",
  "data": {
    "status": "COMPLETED",
    "summary": {
      "featuresGenerated": 3,
      "scenariosGenerated": 12,
      "totalSteps": 48,
      "passed": 11,
      "failed": 1,
      "duration": "PT5M30S"
    }
  },
  "timestamp": "2026-01-28T10:25:00Z"
}
```

#### JOB_FAILED

```json
{
  "type": "JOB_FAILED",
  "jobId": "550e8400-...",
  "data": {
    "status": "FAILED",
    "errorCode": "GENERATION_TIMEOUT",
    "message": "Generation timed out after 30 minutes",
    "failedAt": "GENERATING",
    "lastSuccessfulScenario": "abc123-..."
  },
  "timestamp": "2026-01-28T10:30:00Z"
}
```

---

## Endpoint de Polling (Alternativa)

<aside>
➡️

**GET** `/v1/generation-jobs/{jobId}/progress`

</aside>

Retorna o estado atual do progresso.

### Response

```json
{
  "jobId": "550e8400-...",
  "status": "GENERATING",
  "progress": {
    "completedScenarios": 5,
    "totalScenarios": 12,
    "completedFeatures": 1,
    "totalFeatures": 3,
    "percentage": 41.67
  },
  "currentActivity": {
    "type": "GENERATING_SCENARIO",
    "featureName": "Gestão de Clientes",
    "scenarioTitle": "Criar cliente com dados válidos"
  },
  "recentEvents": [
    {
      "type": "SCENARIO_COMPLETED",
      "scenarioId": "abc123-...",
      "title": "Login com credenciais válidas",
      "timestamp": "2026-01-28T10:15:30Z"
    }
  ],
  "estimatedTimeRemaining": "PT7M",
  "startedAt": "2026-01-28T10:10:00Z"
}
```

---

## Endpoint para Histórico de Eventos

<aside>
➡️

**GET** `/v1/generation-jobs/{jobId}/events`

</aside>

Retorna todos os eventos do Job para reconstrução do histórico.

### Query Parameters

| Param | Tipo | Descrição | Default |
| --- | --- | --- | --- |
| `since` | Timestamp | Eventos após este timestamp | - |
| `type` | String | Filtrar por tipo de evento | - |
| `limit` | Integer | Máximo de eventos | 100 |

### Response

```json
{
  "jobId": "550e8400-...",
  "events": [
    {
      "id": "evt-001",
      "type": "JOB_STATUS_CHANGED",
      "data": { "previousStatus": "CREATED", "currentStatus": "PLANNING" },
      "timestamp": "2026-01-28T10:00:00Z"
    },
    {
      "id": "evt-002",
      "type": "JOB_STATUS_CHANGED",
      "data": { "previousStatus": "PLANNING", "currentStatus": "PLANNED" },
      "timestamp": "2026-01-28T10:05:00Z"
    },
    {
      "id": "evt-003",
      "type": "SCENARIO_COMPLETED",
      "data": { "scenarioId": "...", "title": "Login com credenciais válidas" },
      "timestamp": "2026-01-28T10:15:30Z"
    }
  ],
  "hasMore": false,
  "nextCursor": null
}
```

---

## Componentes de UI Sugeridos

### 1. Progress Overview

```
┌─────────────────────────────────────────────────────────┐
│  Geração de Testes                          ⏱️ 5m 30s   │
│                                                         │
│  ████████████░░░░░░░░░░░░░░░░░░░░░░░░░  41%            │
│                                                         │
│  📊 5/12 Scenarios  |  1/3 Features                     │
└─────────────────────────────────────────────────────────┘
```

### 2. Current Activity

```
┌─────────────────────────────────────────────────────────┐
│  🔄 Gerando: Criar cliente com dados válidos            │
│     Feature: Gestão de Clientes                         │
└─────────────────────────────────────────────────────────┘
```

### 3. Completed Items (Timeline)

```
┌─────────────────────────────────────────────────────────┐
│  ✅ Login com credenciais válidas          10:15:30     │
│  ✅ Login com senha incorreta              10:15:45     │
│  ✅ Login com usuário inexistente          10:16:00     │
│  ✅ [Feature] Autenticação de Usuários     10:16:00     │
│  🔄 Criar cliente com dados válidos        em andamento │
└─────────────────────────────────────────────────────────┘
```

### 4. Status Badge

| Status | Badge | Cor |
| --- | --- | --- |
| `PLANNING` | 🧠 Planejando | Azul |
| `WAITING_APPROVAL` | ⏳ Aguardando Aprovação | Amarelo |
| `GENERATING` | ⚙️ Gerando | Azul |
| `VALIDATING` | 🧪 Validando | Roxo |
| `COMPLETED` | ✅ Concluído | Verde |
| `FAILED` | ❌ Falhou | Vermelho |

---

## Comportamentos de Reconexão

### WebSocket

```
Desconexão detectada
        │
        ▼
   Aguardar 1s
        │
        ▼
   Tentar reconectar
        │
    ┌───┴───┐
    │       │
 Sucesso  Falha
    │       │
    ▼       ▼
 Buscar   Backoff
 eventos  exponencial
 perdidos (2s, 4s, 8s...)
```

### Polling

```
Falha na requisição
        │
        ▼
   Incrementar intervalo
   (max 30s)
        │
        ▼
   Retry
        │
    ┌───┴───┐
    │       │
 Sucesso  Falha
    │       │
    ▼       ▼
 Resetar  Continuar
 intervalo backoff
```

---

## Regras de Negócio

| Código | Regra | Racional | errorCode |
| --- | --- | --- | --- |
| RN10.05.1 | Usuário deve ter permissão no TestProject | Segurança | `INSUFFICIENT_PERMISSIONS` |
| RN10.05.2 | WebSocket desconecta se Job completar/falhar | Economia de recursos | - |
| RN10.05.3 | Eventos são retidos por 24h após conclusão | Histórico | - |
| RN10.05.4 | Máximo de 5 conexões WebSocket por Job | Rate limiting | `TOO_MANY_CONNECTIONS` |

---

## Entidades Relacionadas

### GenerationJobEvent (NOVA)

| Campo | Tipo | Descrição |
| --- | --- | --- |
| `id` | UUID | Identificador único |
| `jobId` | UUID | FK para GenerationJob |
| `type` | Enum | Tipo do evento |
| `data` | JSONB | Payload do evento |
| `timestamp` | Timestamp | Quando ocorreu |

---

## Resultado Esperado

- ✅ Progresso visível por Scenario (não por STEP)
- ✅ Timeline de eventos em tempo quase real
- ✅ Indicadores de progresso (%, contagem, tempo)
- ✅ Feedback do que está sendo gerado no momento
- ✅ Histórico de eventos para reconstrução
- ✅ Reconexão automática em caso de falha
