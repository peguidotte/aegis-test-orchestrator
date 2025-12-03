# **Especificação MVP: Aegis-Test (Homolog Core)**

## **I. Objetivo Consolidado do MVP**

Provar a capacidade do **Homolog Orchestrator** de:

* Receber um requisito em linguagem natural.
* Traduzir isso em um artefato de teste estruturado (**metadados + código Karate DSL**) via *Dual AI*.
* Persistir esse artefato no banco para edição no frontend.

O MVP deve validar:

* Comunicação e tipagem entre **Spring (aegis-orchestrator-test)** e **Python (aegis-analysis-agent)**.
* Geração de um **JSON estruturado (ScenarioDraft)** a partir do requisito.
* Persistência correta de **test_scenarios, api_calls, domains, tags**.

---

## **II. Arquitetura de Geração e Persistência**

A geração é orquestrada pelo **Spring**, mas executada pelos **Agentes Python**.

### **Módulos**

| Módulo               | Tecnologia   | Função                                                       |
| -------------------- | ------------ | ------------------------------------------------------------ |
| Frontend             | Next.js      | Enviar `userPrompt` e `swaggerUrl` ao Spring                 |
| Orchestrator         | Spring Boot  | Receber requisição, chamar Agentes, persistir JSON e Gherkin |
| Agente 1 (Estrutura) | Python + LLM | Gerar JSON **ScenarioDraft**                                 |
| Agente 2 (Código)    | Python + LLM | Gerar Karate DSL (**generatedGherkin**)                      |

---

## **III. Modelo de Dados (Entidades do MVP)**

| Coleção            | Propósito                 | Campos Chave                                               |
| ------------------ | ------------------------- | ---------------------------------------------------------- |
| **test_projects**  | Isolamento e contexto     | `projectId`                                                |
| **domains**        | Organização funcional     | `domainId`, `name`                                         |
| **tags**           | Categorizar testes        | `tagId`, `name`                                            |
| **api_calls**      | Componentes reutilizáveis | `callId`, `routeDefinition`, `baseGherkin`                 |
| **test_scenarios** | Artefato principal        | `scenarioId`, `title`, `generatedGherkin`, `abstractModel` |

⚠️ **Ponto chave:**
`generatedGherkin` (TEXT) em **test_scenarios** permite edição Low-Code ou High-Code via UI.

---

## **IV. Fluxo de Geração End-to-End (Assíncrono Controlado)**

A geração ocorre em **duas etapas**, com callbacks dos agentes.

### **Fluxo**

1. **Usuário inicia geração**
   *Frontend → Spring*
   `POST /generation/start`

2. **Spring armazena o pedido e chama Agente 1**

3. **Agente 1 gera JSON estruturado (ScenarioDraft)**
   Exemplo:

   ```json
   {
     "title": "Criação de Cliente...",
     "apiCallDefinitions": [...]
   }
   ```

4. **Agente 1 envia callback para o Spring**
   `POST /generation/continue/{tempId}`

5. **Spring cria metadados e registros de api_calls**

6. **Spring chama Agente 2 com o JSON + IDs**

7. **Agente 2 gera o Karate DSL**

8. **Agente 2 envia callback final**
   `POST /generation/finish/{scenarioId}`

9. **Spring persiste o `generatedGherkin` e responde ao Frontend**

---

## **V. Próximos Passos (Foco em Desenvolvimento)**

### **Tarefa Crítica Atual: Mapeamento de Dados e API**

1. **DTO de Entrada**

   * Classe para receber `projectId`, `userPrompt`, `swaggerUrl`.

2. **DTO de Saída (Agente 1 → Spring)**

   * Deve mapear exatamente o JSON do **ScenarioDraft**.

3. **Endpoints do GenerationController**

   * `startGeneration`
   * `continueGeneration`
   * `finishGeneration`

---

## **Ordem de Desenvolvimento (Priorização)**

### **FASE 1 — Contrato e Persistência (Mocked)**

Repositório: **aegis-orchestrator-test**

* Modelagem das entidades e repositories.
* Implementar 3 endpoints.
* Criar mocks para os Agentes.

### **FASE 2 — Agentes de IA**

Repositório: **aegis-analysis-agent**

* Desenvolver Agente 1 (JSON).
* Desenvolver Agente 2 (Gherkin).
* Ligar Spring → Python de verdade.

### **FASE 3 — Frontend e Execução**

Repositórios: **homologger-ui**, Infra GCloud

* Construir UI.
* Implementar execução (Cloud Run).