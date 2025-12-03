# **Modelo de Dados**

O modelo final segue a hierarquia **Organização > Time > Projeto** para garantir o isolamento total de contexto e código. O **Projeto** é a unidade de isolamento para todos os artefatos de teste.

## **Estrutura de Isolamento**

### ORGANIZATION

| **Campo** | **Tipo** | **Descrição** |
| --- | --- | --- |
| organizationId | String (PK) | ID Único da Organização (Ex: CERC, Cliente X). |
| name | String | Nome da Organização. |
| type | String | Tipo de organização ('internal_cerc' ou 'external_client'). |

### TEAMS

| **Campo** | **Tipo** | **Descrição** |
| --- | --- | --- |
| teamId | String (PK) | ID Único do Time (Ex: 'squad-liquidacao'). |
| organizationId | String (FK) | Chave estrangeira para a Organização (MACRO). |
| name | String | Nome do Time/Squad. |
| gcloudProjectId | String (Opcional) | ID do projeto GCloud para variáveis de ambiente. |

### PROJECTS

| **Campo** | **Tipo** | **Descrição** |
| --- | --- | --- |
| projectId | String (PK) | ID Único do Projeto/Repositório de Teste. |
| teamId | String (FK) | Chave estrangeira para o Time responsável. |
| name | String | Nome do Projeto (Ex: 'Regressão HML Interna'). |
| scope | String | Define o escopo de teste: ('internal_systems', 'client_homologation'). |
| description | String | Descrição do Projeto. |
| createdAt | Timestamp | Data de criação. |
| updatedAt | Timestamp | Última atualização. |
| createdBy | String (FK) | Criado por quem. |
| lastUpdatedBy | String (FK) | Modificado por quem. |

## **Metadados e Configuração**

### DOMAINS

| **Campo** | **Tipo** | **Descrição** |
| --- | --- | --- |
| domainId | String (PK) | ID Único do Domínio. |
| name | String | Nome do Domínio (Ex: 'Users', 'Titles', 'Liquidation'). |
| description | String | Descrição detalhada do escopo do domínio. |
| createdAt | Timestamp | Data de criação. |
| updatedAt | Timestamp | Última atualização. |
| createdBy | String (FK) | Criado por quem. |
| lastUpdatedBy | String (FK) | Modificado por quem. |

### TAGS

| **Campo** | **Tipo** | **Descrição** |
| --- | --- | --- |
| tagId | String (PK) | ID Único da Tag. |
| name | String | Nome da Tag (Ex: 'smoke', 'regressao', 'borda'). |
| description | String | Descrição do uso da tag. |
| level | String | Nível de Aplicação ('feature', 'scenario', 'both'). |
| createdAt | Timestamp | Data de criação. |
| updatedAt | Timestamp | Última atualização. |
| createdBy | String (FK) | Criado por quem. |
| lastUpdatedBy | String (FK) | Modificado por quem. |

### ENVIRONMENTS

| **Campo** | **Tipo** | **Descrição** |
| --- | --- | --- |
| environmentId | String (PK) | ID Único do Ambiente. |
| projectId | String (FK) | Chave estrangeira para o dono do Ambiente. |
| name | String | Nome do Ambiente (Ex: 'HML', 'Staging', 'Sandbox'). |

### BASE_URLS

| **Campo** | **Tipo** | **Descrição** |
| --- | --- | --- |
| baseUrlId | String (PK) | ID único da baseUrl |
| environmentId | String (FK) | Chave estrangeira para environment da url. |
| projectId | String (FK) | Chave estrangeira para o dono da baseUrl. |
| path | String | Path da base URL |

### GLOBAL_VARIABLES

| **Campo** | **Tipo** | **Descrição** |
| --- | --- | --- |
| variableId | String (PK) | ID Único da Variável. |
| projectId | String (FK) | Chave de isolamento. |
| name | String | Nome da Variável (Ex: 'AUTH_TOKEN', 'DEFAULT_CNPJ'). |
| value | String | O valor real da variável. |
| environmentId | String (FK/Opcional) | Se preenchido, a variável é **específica** deste ambiente. Se nulo, é **global** a todos. |
| isSecret | Boolean | Indica se a variável deve ser tratada como segredo (não logada). |

### **EXECUTION_SETTINGS**

Centraliza as configurações que definem *como* e *quais* testes serão executados.

| **Campo** | **Tipo** | **Descrição** |
| --- | --- | --- |
| settingId | String (PK) | ID Único da Configuração de Execução. |
| projectId | String (FK) | Chave de isolamento. |
| name | String | Nome da configuração. |
| batchSize | Integer | Macro-Paralelismo: Max testes por Lote/Instância do Cloud Run. |
| parallelismThreads | Integer | Micro-Paralelismo: Threads internas do Karate. |
| featuresToExecute | Array | Array de IDs de featureId a serem incluídas (opcional). |
| tagsToInclude | Array | Array de IDs de tagId para inclusão na execução. |
| tagsToExclude | Array | Array de IDs de tagId para exclusão na execução. |
| environmentId | String (FK) | Ambiente alvo da execução. |
| reportFormat | String | Formato do relatório (Ex: 'HTML', 'JSON', 'JIRA'). |
| jiraProjectKey | String | Chave do projeto JIRA para integração automática. |

## **Resultados e Auditoria de Execução**

### **EXECUTIONS**

| **Campo** | **Tipo** | **Descrição** |
| --- | --- | --- |
| **executionId** | **String (PK)** | **ID Único da Execução (Registro de Evento).** |
| settingId | String (FK) | Chave estrangeira para a configuração usada. |
| projectId | String (FK) | Chave de isolamento. |
| startTime | Timestamp | Início do disparo. |
| endTime | Timestamp | Fim da execução total. |
| **triggerType** | **String** | **Fonte do disparo ('CRON', 'MANUAL', 'PIPELINE').** |
| **triggerContext** | **JSON** | **Contexto do disparo (Ex: { 'userEmail': 'x', 'prId': 123, 'branch': 'feat' }).** |
| executedBy | String | ID do usuário que disparou (se triggerType for 'MANUAL'). |
| overallStatus | String | Status final ('PASS', 'FAIL', 'ABORTED'). |
| totalScenarios | Integer | Número total de cenários rodados. |
| passedScenarios | Integer | Número de cenários que passaram. |

### **EXECUTION_RESULTS**

Armazena os logs detalhados e o resultado individual de cada cenário para a IA analisar.

| **Campo** | **Tipo** | **Descrição** |
| --- | --- | --- |
| resultId | String (PK) | ID Único do Resultado. |
| **executionId** | **String (FK)** | **Chave estrangeira para o disparo de executions.** |
| scenarioId | String (FK) | Chave estrangeira para o Cenário executado. |
| status | String | Status individual ('PASS', 'FAIL'). |
| logContent | TEXT | Log de execução completo do Karate (JSON/TXT). **Fonte primária para a IA.** |
| failureMessage | String | Mensagem de erro resumida (se falhou). |
| durationMs | Integer | Tempo de execução do cenário em milissegundos. |
| jiraIssueId | String (Opcional) | ID do Card JIRA criado pela IA para este erro. |

### **EXECUTION_ANALYSIS**

Armazena a inteligência e o diagnóstico da IA sobre a execução, incluindo a comparação temporal.

| **Campo** | **Tipo** | **Descrição** |
| --- | --- | --- |
| analysisId | String (PK) | ID Único da Análise. |
| **executionId** | **String (FK)** | **Execução analisada.** |
| **scenarioId** | **String (FK/Opcional)** | **Cenário específico analisado (para análise granular).** |
| **aiDiagnosis** | **TEXT** | **Resultado da análise da IA (Ex: 'Falha de Auth, token expirado').** |
| **temporalComparison** | **JSON** | **Análise da falha (Ex: '{ 'previous_status': 'PASS', 'historyCount': 5 }').** |
| jiraIssueId | String (Opcional) | ID do Card JIRA criado pela IA para este erro. |
| analysisTimestamp | Timestamp | Quando a análise foi gerada. |

## **Artefatos de Teste e Código**

### TEST_FEATURES

| **Campo** | **Tipo** | **Descrição** |
| --- | --- | --- |
| featureId | String (PK) | ID Único da Feature (Suite). |
| projectId | String (FK) | Chave de isolamento. |
| title | String | Título legível da Feature. |
| domainId | String (FK) | Chave estrangeira para o domínio principal. |
| tags | Array | Array de IDs de Tags aplicadas no nível Feature. |
| createdAt | Timestamp | Data de criação. |
| totalExecutions | Integer | Contador total de vezes que esta Feature foi executada. |
| totalFailures | Integer | Contador total de vezes que esta Feature falhou. |
| failureRate | Float | Taxa de falha (Ex: 0.0−1.0), calculada como totalFailures / totalExecutions. |

### TEST_SCENARIOS

| **Campo** | **Tipo** | **Descrição** | Required | Default |
| --- | --- | --- | --- | --- |
| scenarioId | String (PK) | ID Único do Cenário. | Sim | Sem |
| featureId | String (FK) | Chave estrangeira para a Feature à qual pertence. | Sim | Sem |
| title | String | Título legível do Cenário. | Sim | Sem |
| tagIds | Array | Array de IDs de Tags aplicadas no nível Cenário. | Não | Sem |
| customVariableIds | Array | Array de FKs de variableId a serem injetadas/sobrepostas neste Cenário. | Não | Sem |
| usedApiCallIds | Array | Array de FKs de callId utilizados neste Cenário (Rastreamento). | Não | Sem |
| abstractModel | JSON/TEXT | A representação do Cenário em blocos Drag-and-Drop (Modelo Low-Code). | Sim | Sem |
| generatedGherkin | TEXT | O Gherkin puro do Cenário. | Sim | Sem |
| updatedAt | Timestamp | Última modificação. | Sim | time.now |
| createdAt | Timestamp | Data de criação. | Sim | time.now |
| createdBy | String (FK) | Criado por quem. | Sim | AutoAI |
| lastUpdatedBy | String (FK) | Modificado por quem | Não | AutoAI |
| totalExecutions | Integer | Contador total de vezes que este Cenário foi executado. | Sim | 0 |
| totalFailures | Integer | Contador total de vezes que este Cenário falhou. | Sim | 0 |
| failureRate | Float | Taxa de falha (0.0−1.0), calculada como totalFailures / totalExecutions. | Sim | 0 |

### API_CALLS

| **Campo** | **Tipo** | **Descrição** | Required | Default |
| --- | --- | --- | --- | --- |
| callId | String (PK) | ID Único da Chamada. | Sim | Sem |
| projectId | String (FK) | Chave de isolamento. | Sim | Sem |
| domainId | String (FK) | Chave estrangeira para o domínio ao qual a chamada pertence. | Sim | Sem |
| baseUrlId | String (FK) | Chave estrangeira para a baseUrl usada | Sim | Sem |
| routeDefinition | String | A rota da API (Ex: v1/users). | Sim | Sem |
| method | String | Método HTTP (Ex: POST). | Sim | Sem |
| customVariables | Array | Array de FKs de variableId a serem injetadas/sobrepostas nesta Chamada. | Não | Sem |
| baseGherkin | TEXT | O Gherkin base do componente (a chamada real com *request/response*). | Sim | Sem |
| requiredParams | JSON | Parâmetros de entrada esperados. | Não | Sem |
| updatedAt | Timestamp | Última modificação. | Sim | time.now |
| createdAt | Timestamp | Data de criação. | Sim | time.now |
| createdBy | String (FK) | Criado por quem. | Sim | AutoAI |
| lastUpdatedBy | String (FK) | Modificado por quem | Não | AutoAI |

### CUSTOM_SNIPPETS

| **Campo** | **Tipo** | **Descrição** | Required | Default |
| --- | --- | --- | --- | --- |
| snippetId | String (PK) | ID Único do Snippet. | Sim | Sem |
| projectId | String (FK) | Chave de isolamento. | Sim | Sem |
| name | String | Nome de chamada do snippet (Ex: 'funcaoCriptografia'). | Sim | Sem |
| language | Enum | Linguagem do código ('js', 'java', 'kotlin'). | Sim | Sem |
| codeContent | TEXT | O código-fonte puro do snippet. | Sim | Sem |
| scope | Enum | Escopo de uso ('global' para todos os testes, 'local' para testes específicos). | Sim | Global |
| isVerified | Boolean | Indicador de que o código foi revisado. | Não | Sem |
| createdBy | String | ID do usuário que criou o snippet. | Sim | time.now |
| lastModifiedBy | String | ID do último usuário a modificar. | Sim | time.now |