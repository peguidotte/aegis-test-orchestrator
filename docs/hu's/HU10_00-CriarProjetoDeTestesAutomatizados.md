> COMO: Administrador de Projeto (Core)
QUERO: Inicializar um módulo de testes dentro de um projeto existente
PARA: Organizar suites, ambientes e especificações de testes automatizados de forma isolada.
>

---

<aside>
➡️

Método: **POST /v1/projects/{projectId}/test-projects** (PROTEGIDO)

</aside>

- Descrição do Endpoint

  Cria a entidade raiz do Aegis Tests. Este TestProject servirá como o container para Environments, API Calls e Specifications. Ele herda as permissões de membros do Projeto Core.


## Layouts

### Request

| **Nome do campo** | **Tipo do campo** | **Descrição do campo** | **Exemplo do campo** | **Requisito** |
| --- | --- | --- | --- | --- |
| `name` | String (255) | Nome identificador do módulo de teste. | `Suíte de Testes - Recebíveis` | Obrigatório |
| `description` | String (1000) | Breve descrição do escopo. | `Testes de integração da API de Duplicatas` | Opcional |

```json
{
  "name": "Suíte de Testes - Recebíveis",
  "description": "Testes de integração da API de Duplicatas"
}
```

### Response

| **Nome do campo** | **Tipo do campo** | **Descrição do campo** | **Exemplo do campo** | **Requisito** |
| --- | --- | --- | --- | --- |
| `id` | Long | ID do TestProject criado. | `19291830192312` | Obrigatório |
| `createdAt` | Timestamp | Data de criação. | `2026-01-12T14:00:00Z` | Obrigatório |

```json
{
  "id": 500,
  "name": "Suíte de Testes - Recebíveis",
  "projectId": 10,
  "createdAt": "2026-01-12T14:00:00Z",
  "createdBy": "Pedro Guidotte"
}
```

## Regras de campo

A validação de campo deve ser executada primeiro, retornando `400 Bad Request` se qualquer uma for violada.

| **Código** | **Campo(s) envolvido(s)** | **Regra de campo** | **Racional** | **errorCode** |
| --- | --- | --- | --- | --- |
| RC10.01.1 | `name` | Não pode ser vazio e deve ter max 255 caracteres. | Integridade de exibição. | `INVALID_FIELD_LENGTH` |
| RC10.01.2 | `description` | Se existir, deve conter no max 1000 caracteres | Integridade de exibição | `INVALID_FIELD_LENGTH` |

## Regras de negócio

As regras de negócio (RNs) geralmente resultam em `401 Unauthorized`, `403 Forbidden` , `409 Conflict` ou `422 Unprocessable Entity`

| **Código** | **Campo(s) envolvido(s)** | **Regra de Negócio** | **Racional** | **errorCode** |
| --- | --- | --- | --- | --- |
| RN10.01.1 | `projectId` | O Projeto Core deve existir e o usuário deve ser ADMIN/OWNER. | Governança de acesso. | `PROJECT_NOT_FOUND` |
| RN10.01.2 | `limit` | Cada Projeto Core só pode ter 1 TestProject (MVP, isso será alterado depois com base em planos). | Política de plano e billing. | `TEST_PROJECT_LIMIT_REACHED` |
| RN10.01.3 | `name` | Não deve existir dentro de um PROJECT dois TESTPROJECTS com mesmo nome | Respeitar responsabilidade e boas práticas. | `TEST_PROJECT_NAME_ALREADY_EXISTS` |

## Respostas HTTP

| **Categoria** | **Código HTTP** | **Descrição** | **Motivo de Retorno** |
| --- | --- | --- | --- |
| Sucesso | 201 Created | TestProject criado. | Sucesso na inicialização. |
| Erro Cliente | 403 Forbidden | Acesso negado. | Usuário não é Admin do projeto. |

## Fluxo técnico

1. Validação de existência do Projeto Core.
2. Persistência na tabela `T_AEGIS_TEST_PROJECTS`.
3. Criação automática de um Environment padrão chamado "Default".