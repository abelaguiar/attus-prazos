# Monitor de Prazos Processuais

Aplicação full-stack para cadastrar e acompanhar prazos de processos: dá pra registrar um
prazo, editar, marcar como cumprido e ver quais já venceram. O domínio é inspirado em
procuradoria digital.

Foi feito como teste técnico, então cobre o fluxo inteiro (front, API, banco e logs) e inclui
uma análise de incidente em [`docs/INCIDENT_ANALYSIS_LOST_UPDATE.md`](docs/INCIDENT_ANALYSIS_LOST_UPDATE.md).

## Stack

| Camada | Tecnologia |
|---|---|
| Back-end | Java 21, Spring Boot 4 (Web MVC, Data JPA, Bean Validation) |
| Banco | PostgreSQL via Docker; H2 em memória para rodar local e nos testes |
| Logs | SLF4J + Logback em JSON (ECS), com um `requestId` por requisição |
| Testes | JUnit 5, Mockito, Spring MockMvc |
| Front-end | React 19 + TypeScript + Vite |
| Infra | Docker e Docker Compose |

## Arquitetura

O back-end é organizado em camadas:

```
HTTP → Controller → Service → Repository → Banco
```

- `web/`: controllers, DTOs, tratamento de erro e o filtro de requestId. Recebe o HTTP e
  devolve JSON, sem regra de negócio.
- `service/`: regra de negócio e controle de transação.
- `repository/`: acesso a dados com Spring Data JPA.
- `domain/`: a entidade `Prazo` e o que é dela (por exemplo, "vencido" é calculado, não tem
  coluna).

Os erros passam por um handler global, que devolve 400/404/409/500 num formato consistente.

## Como executar

Pré-requisitos: Java 21, Node 20+ e, se quiser rodar via container, Docker. O Maven não é
obrigatório porque o projeto traz o wrapper (`./mvnw`).

### Com Docker

```bash
docker compose up --build
```

- Front: <http://localhost:5173>
- API: <http://localhost:8080>
- Banco: PostgreSQL no container `postgres`, selecionado pelo profile `docker`.

As credenciais e a porta do Postgres têm valores padrão, mas dá pra sobrescrever por variável
de ambiente (útil quando a 5432 já está em uso, ou pra não deixar senha no repositório):

```bash
DB_PORT=5433 POSTGRES_PASSWORD=secret docker compose up
```

Variáveis disponíveis: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` (padrão `prazos`) e
`DB_PORT` (porta publicada no host, padrão `5432`).

### Local (sem Docker)

Back-end, na porta 8080:

```bash
cd backend
./mvnw spring-boot:run
```

Front, na porta 5173, em outro terminal:

```bash
cd frontend
npm install
npm run dev
```

Sem Docker a aplicação usa H2 em memória, então não precisa instalar banco. Os testes também
rodam no H2:

```bash
cd backend
./mvnw test
```

## API

Base: `http://localhost:8080`

| Método | Rota | Descrição | Sucesso | Erros |
|---|---|---|---|---|
| `POST` | `/prazos` | Cria um prazo | `201` | `400` validação, `409` duplicado |
| `GET` | `/prazos` | Lista os prazos | `200` | — |
| `GET` | `/prazos/{id}` | Busca um prazo | `200` | `404` |
| `PUT` | `/prazos/{id}` | Edita um prazo (exige `version`) | `200` | `400`, `404`, `409` conflito de versão |
| `PATCH` | `/prazos/{id}/cumprir` | Marca como cumprido | `200` | `404` |

Toda resposta traz o header `X-Request-Id`, que casa com os logs. O campo `version` é devolvido
em todas as respostas e precisa ser enviado de volta no `PUT` (é o controle de concorrência).

Criar um prazo:

```bash
curl -X POST http://localhost:8080/prazos \
  -H "Content-Type: application/json" \
  -d '{"numeroProcesso":"0001234-56.2026.8.26.0100","descricao":"Contestação","dataPrazo":"2026-12-31"}'
```

Resposta (`201`):

```json
{
  "id": 1,
  "numeroProcesso": "0001234-56.2026.8.26.0100",
  "descricao": "Contestação",
  "dataPrazo": "2026-12-31",
  "status": "PENDENTE",
  "vencido": false,
  "criadoEm": "2026-06-18T20:00:00Z",
  "cumpridoEm": null,
  "version": 0
}
```

Quando a entrada é inválida, o 400 vem com o detalhe de cada campo:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Erro de validação",
  "fieldErrors": [
    { "field": "descricao", "message": "descricao é obrigatória" }
  ]
}
```

Tentar criar um prazo igual a outro (mesmo processo, descrição e data) dá 409:

```json
{ "status": 409, "error": "Conflict", "message": "Já existe um prazo com este processo, descrição e data." }
```

Para editar, mande o `version` que veio na leitura:

```bash
curl -X PUT http://localhost:8080/prazos/1 \
  -H "Content-Type: application/json" \
  -d '{"descricao":"Apelação","dataPrazo":"2027-01-15","version":0}'
```

Se outra pessoa editou o prazo nesse meio tempo, o `version` enviado fica defasado e a edição
é recusada com 409, em vez de gravar por cima:

```json
{ "status": 409, "error": "Conflict", "message": "O prazo 1 foi modificado por outra operação. Recarregue e tente novamente." }
```

## Logs

Os logs vão para `backend/logs/prazos.json` em JSON (padrão ECS), cada linha com o `requestId`
da requisição (propagado por MDC e devolvido no header `X-Request-Id`). Com isso dá pra seguir
uma requisição do começo ao fim. Exemplo de uma linha:

```json
{ "@timestamp": "...", "log": {"level":"INFO"}, "message": "Prazo criado id=1 ...", "requestId": "0fff2b34-..." }
```

No console o formato fica legível, para ajudar no dia a dia de desenvolvimento.

## Estrutura do projeto

```
attus-prazos/
├── backend/            # API Spring Boot
│   └── src/main/java/com/attus/prazos/
│       ├── domain/         # Prazo, StatusPrazo
│       ├── repository/     # PrazoRepository (Spring Data JPA)
│       ├── service/        # PrazoService
│       └── web/            # Controller, DTOs, ExceptionHandler, RequestIdFilter
├── frontend/           # React + TypeScript (Vite)
│   └── src/
│       ├── components/     # PrazoForm, PrazoEditForm, PrazoList
│       ├── api.ts          # cliente HTTP
│       └── types.ts        # tipos que espelham o contrato da API
├── docs/
│   ├── INCIDENT_ANALYSIS_LOST_UPDATE.md   # análise de incidente (Parte 2)
│   └── TECH_NOTES.md                      # decisões técnicas e melhorias
└── docker-compose.yml
```

## Mais documentação

- [Análise de incidente — lost update](docs/INCIDENT_ANALYSIS_LOST_UPDATE.md)
- [Nota técnica — decisões e trade-offs](docs/TECH_NOTES.md)
