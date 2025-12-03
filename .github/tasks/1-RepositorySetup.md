# Task 1 - RepositorySetup

## Objetivo
Garantir que o serviço possua entidades JPA e repositories capazes de refletir fielmente o recorte inicial do `data_model.md` (Projects, Domains, Tags, ApiCalls, TestScenarios), preparando o terreno para os fluxos descritos no MVP.

## Plano Numerado
1. Consolidar requisitos do modelo de dados
1.1 Revisar `docs/data_model.md` para confirmar campos obrigatórios e relacionamentos entre Project, Domain, Tag, ApiCall e TestScenario.
1.2 Identificar gaps onde o MVP precisa de campos adicionais (timestamps, autores) e documentar no plano para validação antes de codar.

2. Desenhar entidades JPA alinhadas ao Oracle (facilitando futura migração para Spanner)
2.1 Definir `@Entity` + `@Table(name = "T_AEGIS_...")` para cada agregado, mantendo `String` como PK onde especificado.
2.2 Configurar `@SequenceGenerator` apenas quando necessário (para campos numéricos opcionais) e revisar se PKs string usarão UUID gerado pela aplicação.
2.3 Mapear relacionamentos essenciais (ex.: `Project` -> `TestScenario`, `Domain` -> `ApiCall`) com atenção a `fetch` e cascades mínimos.
2.4 Garantir imutabilidade onde possível (records + `@Builder`/`@With`) sem comprometer JPA; documentar se for preciso cair para classes típicas.

3. Criar interfaces Spring Data JPA
3.1 Criar `Repository` dedicado para cada entidade, expondo operações básicas (`JpaRepository`).
3.2 Adicionar métodos de consulta essenciais que serão usados pelos próximos endpoints (ex.: `findByProjectIdAnd...`).
3.3 Nomear pacotes conforme convenção `com.aegis.homolog.orchestrator.repository`.

4. Definir estratégia de testes (TDD)
4.1 Planejar testes unitários (ou slice tests com `@DataJpaTest`) que validem mapeamentos chave e consultas customizadas.
4.2 Garantir que esses testes utilizem um banco em memória (H2) configurado para simular Oracle, evitando dependência do ambiente da universidade.
4.3 Documentar fixtures/dados seeds mínimos para reutilização futura.

5. Critérios de Aceite
5.1 Todos os repositories compilam e passam nos testes `@DataJpaTest`.
5.2 As entidades atendem ao contrato de `docs/data_model.md` (nomes, tipos e relacionamentos).
5.3 Documentação do pacote (README curto ou comentários) explica decisões que facilitam migração futura para Spanner.
