# HU10.02 — Preparar e Padronizar Dados para Envio ao Agente

> **COMO:** Sistema (Aegis Tests - internamente)
>
> **QUERO:** Preparar e padronizar os dados necessários para envio ao agente de IA
>
> **PARA:** Garantir comunicação consistente com os agentes, independente da origem da requisição de geração.

---

## Contexto

O **GenerationJob** é um **processo interno não-exposto** que atua como orquestrador e normalizador de dados. Ele é o ponto de convergência para **qualquer forma de geração ou atualização de testes**, independente da origem dos dados:

- Specification manual (HU10.01)
- API-DOCS importado
- Repositório atrelado
- Futuras fontes de input

O Job é responsável por:
- Capturar e normalizar todo o contexto necessário do TestProject
- Validar integridade dos dados
- Criar um snapshot consolidado para o agente
- Disparar o agente de planejamento (IA) com dados padronizados
- Rastrear o progresso da geração

### Conceitos Fundamentais

| Conceito | Descrição |
|----------|-----------|
| **Feature** | Arquivo lógico de testes (agrupa Scenarios relacionados) |
| **Scenario** | Unidade de progresso visível ao usuário |
| **STEP** | Microstep de código dentro de um Scenario (comentários `#STEP{n}` + linhas subsequentes) |

⚠️ **Importante:** STEP não é unidade de progresso em tempo real. Progresso é emitido por Scenario.

---

## Tipos de Entrada (InputSource)

O Job pode ser iniciado a partir de diferentes fontes:

### 1. SPECIFICATION
Utiliza uma Specification manual criada na HU10.01.
- Já possui contexto estruturado
- Endpoint, método, exemplos de request

### 2. API_DOCS
Importação de documentação OpenAPI/Swagger. (Fluxo separado)
- Parsing automático dos endpoints
- Criação de ApiCalls a partir do doc

### 3. REPOSITORY
Análise de código-fonte de um repositório atrelado. (Fluxo separado)
- Integração com GitHub/GitLab
- Análise estática do código

_Observação_: Note que independente do fluxo de entrada de uma solicitação de geração, o processo converge para o mesmo fluxo de planejamento e geração de testes. Eu posso ter um repositório atrelado e criar uma Specification manual, os processos irão convergir para que eu colete todos os insights necessários (API_CALLs, DOMAINS, AUTH_PROFILES, etc) do TestProject e então iniciar o planejamento.

---

## Gatilhos de Entrada (Triggers)

O GenerationJob é iniciado quando **qualquer uma das seguintes ações** ocorre no sistema:

| Gatilho | Origem | Dados Capturados |
| --- | --- | --- |
| Specification criada/aprovada | HU10.01 | specificationId, testProjectId, environmentId |
| API-DOCS importado | (Futuro) | apiDocsId, testProjectId, environmentId |
| Repositório analisado | (Futuro) | repositoryId, testProjectId, environmentId |
| Specification atualizada | HU10.01 | specificationId, testProjectId, environmentId |

> **Ponto crítico:** Qualquer que seja a origem, o GenerationJob normaliza todos os dados em um formato único antes de enviar ao agente.

---

## Fluxo Interno

```
[Evento de Entrada]
      ↓
   GenerationJob
      ↓
[Captura de Contexto]
      ↓
[Normalização de Dados]
      ↓
[Snapshot Consolidado]
      ↓
[Envio Padronizado ao Agente]
```

---

## Dados de Entrada Internos

Quando um GenerationJob é acionado, ele recebe:

| Campo | Tipo | Descrição | Obrigatório |
| --- | --- | --- | --- |
| `inputSource` | Enum | Tipo de origem (SPECIFICATION, API_DOCS, REPOSITORY) | Sim |
| `testProjectId` | Long | ID do projeto | Sim |
| `environmentId` | Long | ID do ambiente alvo | Sim |
| `sourceId` | Long | ID da Specification/ApiDocs/Repository conforme inputSource | Sim |
| `generationMode` | Enum | CREATE ou UPDATE | Sim |
| `generationConfig` | Object | Configurações opcionais de geração | Não |

### GenerationConfig (Opcional)

| Campo | Tipo | Descrição | Default |
| --- | --- | --- | --- |
| `testFramework` | Enum | Framework de testes | `KARATE_DSL` |
| `coverageLevel` | Enum | Nível de cobertura | `STANDARD` |
| `includeNegativeTests` | Boolean | Incluir testes negativos | `true` |
| `includeEdgeCases` | Boolean | Incluir edge cases | `true` |

**(DÚVIDA)** Quais outras configurações de geração são relevantes?

---

## Snapshot Consolidado (Gerado Internamente)

O GenerationJob cria um snapshot único que será enviado ao agente:

```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "testProject": {
    "id": 500,
    "name": "Projeto de Testes",
    "description": "..."
  },
  "environment": {
    "id": 3,
    "name": "Staging",
    "baseUrl": "https://api.staging.example.com"
  },
  "apiCalls": [
    {
      "id": 1,
      "method": "POST",
      "endpoint": "/users",
      "description": "Criar novo usuário",
      "requestExample": { ... },
      "responseExample": { ... },
      "tags": ["user", "creation"]
    }
  ],
  "domains": [
    {
      "id": 10,
      "name": "User",
      "description": "Entidade de usuário",
      "properties": { ... }
    }
  ],
  "authProfiles": [
    {
      "id": 5,
      "name": "JWT Bearer",
      "type": "BEARER",
      "tokenEndpoint": "..."
    }
  ],
  "globalVariables": [
    {
      "id": 20,
      "key": "BASE_TIMEOUT",
      "value": "5000",
      "scope": "GLOBAL"
    }
  ],
  "tags": [ "unit", "smoke", "integration" ],
  "generationConfig": {
    "testFramework": "KARATE_DSL",
    "coverageLevel": "COMPREHENSIVE",
    "includeNegativeTests": true,
    "includeEdgeCases": true
  },
  "sourceContext": {
    "inputSource": "SPECIFICATION",
    "specificationId": 9001,
    "specificationTitle": "Criar Usuário",
    "specificationDescription": "..."
  }
}
```

---

## GenerationJobStatus (Ciclo de Vida)

```
CREATED ──► CAPTURING_CONTEXT ──► PLANNING ──► PLANNED
                                                  │
                    ┌─────────────────────────────┼─────────────────────────┐
                    │                             │                         │
                    ▼                             ▼                         ▼
          WAITING_APPROVAL              APPROVED_WITH_EDITS            REJECTED
                    │                             │                         │
                    └───────────┬─────────────────┘                         │
                                │                                           │
                                ▼                                           │
                            GENERATING ◄────────────────────────────────────┘
                                │
                                ▼
                           VALIDATING
                                │
                                ▼
                           COMPLETED ──► FAILED (em caso de erro crítico)

Qualquer status pode transicionar para ERROR em caso de falha do sistema.
```

| Status | Descrição |
| --- | --- |
| `CREATED` | Job criado, aguardando início |
| `CAPTURING_CONTEXT` | Capturando contexto do TestProject (environments, apiCalls, domains, etc.) |
| `PLANNING` | IA planejando Features e Scenarios |
| `PLANNED` | Planejamento concluído |
| `WAITING_APPROVAL` | Aguardando aprovação do usuário |
| `APPROVED` | Planejamento aprovado |
| `APPROVED_WITH_EDITS` | Planejamento aprovado com edições |
| `REJECTED` | Planejamento rejeitado |
| `GENERATING` | IA gerando código de testes |
| `VALIDATING` | Executando e validando testes gerados |
| `COMPLETED` | Processo concluído com sucesso |
| `FAILED` | Processo falhou |
| `ERROR` | Erro interno do sistema |

---

## Fluxo Técnico (Interno)

1. **Sistema detecta** um dos gatilhos de entrada (Specification criada, API-DOCS importado, etc.)
2. **GenerationJob criado** com status `CREATED`
3. **Validações internas:**
   - TestProject existe?
   - Environment pertence ao TestProject?
   - Fonte de entrada (Specification/ApiDocs/Repository) é válida?
   - Não há outro Job em andamento para a mesma entrada?
4. **Captura de contexto (assíncrono):**
   - Buscar environments do TestProject
   - Buscar api_calls do TestProject
   - Buscar domains do TestProject
   - Buscar auth_profiles do TestProject
   - Buscar global_variables do TestProject
   - Buscar tags do TestProject
5. **Atualizar status** para `CAPTURING_CONTEXT`
6. **Normalizar dados** em um snapshot único
7. **Atualizar status** para `PLANNING`
8. **Publicar evento** `GENERATION_JOB_STARTED` para o agente de IA
   - Evento contém o snapshot consolidado
9. **Job finaliza sua responsabilidade**
   - Agente de IA assume a partir daqui

---

## Regras de Campo

| Código | Campo | Regra | Racional | errorCode |
| --- | --- | --- | --- | --- |
| RC10.02.1 | `inputSource` | Obrigatório, deve ser valor válido do enum | Determina a origem | `REQUIRED_FIELD` |
| RC10.02.2 | `sourceId` | Obrigatório | Referência da fonte | `REQUIRED_FIELD` |
| RC10.02.3 | `testProjectId` | Obrigatório | Projeto alvo | `REQUIRED_FIELD` |
| RC10.02.4 | `environmentId` | Obrigatório | Ambiente de execução | `REQUIRED_FIELD` |
| RC10.02.5 | `generationMode` | Obrigatório, CREATE ou UPDATE | Tipo de operação | `REQUIRED_FIELD` |

---

## Regras de Negócio

| Código | Regra | Racional | errorCode |
| --- | --- | --- | --- |
| RN10.02.1 | TestProject deve existir | Consistência | `TEST_PROJECT_NOT_FOUND` |
| RN10.02.2 | Environment deve pertencer ao TestProject | Segurança | `ENVIRONMENT_NOT_FOUND` |
| RN10.02.3 | Specification (se inputSource=SPECIFICATION) deve existir e pertencer ao TestProject | Consistência | `SPECIFICATION_NOT_FOUND` |
| RN10.02.4 | Specification deve estar em status compatível (CREATED ou APPROVED) | Fluxo | `SPECIFICATION_INVALID_STATUS` |
| RN10.02.5 | Não pode haver outro Job em andamento para a mesma entrada | Evitar duplicidade | `JOB_ALREADY_IN_PROGRESS` |
| RN10.02.6 | Todos os dados capturados devem ser válidos e não-nulos | Integridade | `INVALID_CONTEXT_DATA` |

---

## Eventos e Notificações

### Evento de Disparo: GENERATION_JOB_STARTED

**Publicado em:** Fila `aegis.generation.started`

**Payload:**
```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "testProjectId": 500,
  "environmentId": 3,
  "inputSource": "SPECIFICATION",
  "snapshot": { ... },
  "timestamp": "2026-01-28T10:00:00Z"
}
```

### Eventos de Status (Internos)

| Evento | Quando | Descrição |
| --- | --- | --- |
| `GENERATION_JOB_STATUS_CHANGED` | Status alterado | Captura transições de status |
| `GENERATION_JOB_FAILED` | Erro ocorre | Job falha antes do envio ao agente |

---

## Tratamento de Erros

Se qualquer validação falhar, o Job é marcado como `FAILED` e um evento de erro é publicado:

```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "FAILED",
  "errorCode": "SPECIFICATION_NOT_FOUND",
  "errorMessage": "Specification com ID 9001 não encontrada ou não pertence ao TestProject",
  "timestamp": "2026-01-28T10:00:00Z"
}
```

O agente de IA **nunca** recebe dados inválidos. Ele só é notificado quando o snapshot está 100% consolidado e validado.

---

## Entidades Relacionadas

### GenerationJob (NOVA)

| Campo | Tipo | Descrição |
| --- | --- | --- |
| `id` | UUID | Identificador único |
| `testProjectId` | Long | FK para TestProject |
| `inputSource` | Enum | Tipo de entrada |
| `specificationId` | Long | FK para Specification (nullable) |
| `apiDocsId` | Long | FK para ApiDocs (nullable) |
| `repositoryId` | Long | FK para Repository (nullable) |
| `environmentId` | Long | FK para Environment |
| `status` | Enum | Status atual |
| `contextSnapshot` | JSONB | Snapshot do contexto capturado |
| `generationConfig` | JSONB | Configurações de geração |
| `errorMessage` | Text | Mensagem de erro (se houver) |
| `createdAt` | Timestamp | Data de criação |
| `createdBy` | String | Usuário que criou |
| `updatedAt` | Timestamp | Última atualização |

---

## Resultado Esperado

- ✅ Job criado e persistido
- ✅ Contexto capturado e normalizado internamente
- ✅ Snapshot consolidado gerado
- ✅ Agente de IA notificado via evento com dados padronizados
- ✅ Sistema pronto para receber planejamento do agente (HU10.03)
- ❌ Nenhuma API exposta ao usuário
- ❌ Nenhum código gerado ainda
- ❌ Nenhuma Feature/Scenario criada ainda

---

## Por que essa abordagem?

1. **Reutilização:** Múltiplas origens (Specification, API-DOCS, Repository) convergem para um único fluxo
2. **Padronização:** Agentes recebem sempre o mesmo formato de dados, evitando bugs surpresa
3. **Manutenibilidade:** Adicionar novas fontes de geração não quebra o contrato com agentes
4. **Rastreabilidade:** Snapshot consolida exatamente o que foi enviado ao agente
5. **Qualidade:** Validações centralizadas antes do agente gastar recursos em dados inválidos

---

## Próximo Passo

Quando o agente recebe o evento `GENERATION_JOB_STARTED`, ele inicia a **HU10.03 — Planejar Features e Scenarios**, que transforma o snapshot em um plano estruturado de testes.
