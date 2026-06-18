# Nota Técnica — Decisões, Trade-offs e Melhorias

Resumo das principais decisões de engenharia do projeto e do raciocínio por trás delas.

## Decisões e trade-offs

### Domínio enxuto (uma entidade)
Modelei apenas `Prazo` (sem relacionamentos JPA). O foco do teste é demonstrar um fluxo
ponta a ponta com qualidade, não a complexidade do modelo. Uma entidade única mantém o código
fácil de ler, testar e defender, sem perder a riqueza necessária (regra de negócio, validação,
estado derivado).
**Trade-off:** menos "vistoso", porém mais sólido e sem over-engineering.

### "Vencido" é estado derivado, não armazenado
Um prazo está vencido quando a data passou e ele ainda está `PENDENTE`. Calculo isso em
tempo de leitura (`Prazo.isVencido()`) em vez de guardar uma coluna `vencido`.
**Por quê:** uma coluna armazenada exigiria um job atualizando o status diariamente — fonte
clássica de inconsistência. Derivar elimina essa classe de bug.

### DTOs separados da entidade
A API expõe `CriarPrazoRequest`/`PrazoResponse`, não a entidade `Prazo` diretamente.
**Por quê:** desacopla o contrato HTTP do modelo de persistência; permite controlar o que
entra/sai e evoluir o banco sem quebrar a API.

### Linguagem ubíqua (português no domínio, inglês na infraestrutura)
Termos de negócio em português (`Prazo`, `numeroProcesso`, `marcarComoCumprido`) porque é a
linguagem do domínio jurídico; termos técnicos em inglês (`Repository`, `Service`,
`findAll`) por serem convenção do Spring.

### Tratamento de erro centralizado
Um `@RestControllerAdvice` global mapeia exceções para respostas consistentes
(400/404/409/500), mantendo os controllers limpos. O handler de integridade é **específico**:
só devolve `409` quando a violação é da constraint de duplicidade — outras violações viram
`500` com log `ERROR`, sem mascarar o erro real.

### Logs estruturados (JSON/ECS) com `requestId`
Logs em JSON no padrão ECS, com um `requestId` por requisição (via MDC, devolvido no header
`X-Request-Id`). Isso torna os logs pesquisáveis e rastreáveis — e foi o que permitiu a
análise da Parte 2. Console fica legível; arquivo fica estruturado.

### Validação em duas camadas
No front (feedback imediato) e no back (Bean Validation — a que vale). O back nunca confia
no cliente.

### Integridade garantida no banco
A unicidade de prazo é garantida por uma constraint `UNIQUE` no banco, não por uma checagem
na aplicação.
**Por quê:** sob concorrência, um `SELECT` antes do `INSERT` tem condição de corrida; só a
constraint garante a invariante de fato. (Detalhes em `INCIDENT_ANALYSIS.md`.)

### H2 em memória
Banco em memória para zero fricção de setup (não exige instalar nada).
**Trade-off:** os dados somem ao reiniciar — aceitável para teste/demo. Em produção, trocar
por PostgreSQL altera apenas a configuração de datasource.

## Melhorias futuras

- **PostgreSQL + migrações versionadas (Flyway/Liquibase):** hoje o schema é gerado pelo
  Hibernate (`ddl-auto`); em produção o schema deveria ser versionado e auditável.
- **Idempotency key** no `POST /prazos`: retries legítimos de rede retornariam o recurso já
  criado em vez de `409`.
- **Documentação interativa da API (OpenAPI/Swagger):** complementaria a documentação do README.
- **Paginação e filtros** em `GET /prazos` (por situação, por vencimento).
- **Autenticação/autorização** (ex.: por procurador) e auditoria de quem cumpriu cada prazo.
- **Cálculo de prazo em dias úteis** (feriados forenses) — regra real do domínio jurídico.
- **Alertas de observabilidade** sobre taxa de 5xx/409 e prazos próximos do vencimento.
- **Testes end-to-end** no front (ex.: Playwright) cobrindo o fluxo completo na UI.
