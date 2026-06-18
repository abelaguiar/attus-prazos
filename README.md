# Monitor de Prazos Processuais

Aplicação full-stack para **cadastro e monitoramento de prazos processuais** — inspirada no
domínio de procuradoria digital (acompanhar prazos de processos, marcar como cumpridos e
visualizar os que estão vencidos).

Projeto desenvolvido como teste técnico, contemplando **desenvolvimento ponta a ponta**
(front-end, API, persistência e logs) e uma **análise de incidente** (ver
[`docs/INCIDENT_ANALYSIS.md`](docs/INCIDENT_ANALYSIS.md)).

---

## Stack

| Camada | Tecnologia |
|---|---|
| Back-end | Java 21, Spring Boot 4 (Web MVC, Data JPA, Bean Validation) |
| Banco | H2 em memória (dev/teste) |
| Logs | SLF4J + logback, **JSON estruturado (ECS)** com `requestId` por requisição |
| Testes | JUnit 5, Mockito, Spring MockMvc |
| Front-end | React 19 + TypeScript + Vite |
| Infra | Docker + Docker Compose |

## Arquitetura

Back-end em camadas (separação de responsabilidades):

```
HTTP → Controller → Service → Repository → Banco (H2)
        (borda)     (regras)   (acesso)
```

- **Controller** (`web/`): recebe HTTP, valida entrada, devolve JSON. Sem regra de negócio.
- **Service** (`service/`): regra de negócio e transações.
- **Repository** (`repository/`): acesso a dados via Spring Data JPA.
- **Domain** (`domain/`): a entidade `Prazo` e suas regras (ex.: "vencido" é derivado).
- **DTOs** (`web/dto/`): contratos de entrada/saída separados da entidade.
- **GlobalExceptionHandler**: tratamento de erro centralizado (400/404/409/500 consistentes).

---

## Como executar

### Pré-requisitos

- **Java 21** e **Maven** (ou use o `./mvnw` incluído)
- **Node 20+** e **npm**
- *(opcional)* **Docker** e **Docker Compose**

### Opção A — Docker (um comando)

```bash
docker compose up --build
```

- Front-end: <http://localhost:5173>
- API: <http://localhost:8080>

### Opção B — Local (desenvolvimento)

**Back-end** (porta 8080):
```bash
cd backend
./mvnw spring-boot:run
```

**Front-end** (porta 5173), em outro terminal:
```bash
cd frontend
npm install
npm run dev
```

Acesse <http://localhost:5173>.

### Rodar os testes (back-end)

```bash
cd backend
./mvnw test
```

---

## API

Base URL: `http://localhost:8080`

| Método | Rota | Descrição | Sucesso | Erros |
|---|---|---|---|---|
| `POST` | `/prazos` | Cria um prazo | `201 Created` | `400` validação, `409` duplicado |
| `GET` | `/prazos` | Lista todos os prazos | `200 OK` | — |
| `GET` | `/prazos/{id}` | Busca um prazo | `200 OK` | `404` não encontrado |
| `PATCH` | `/prazos/{id}/cumprir` | Marca como cumprido | `200 OK` | `404` não encontrado |

Toda resposta inclui o header **`X-Request-Id`** (rastreável nos logs).

### Exemplos

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
  "cumpridoEm": null
}
```

Entrada inválida (`400`) — retorna o detalhe de cada campo:
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

Prazo duplicado (`409`) — mesmo processo + descrição + data:
```json
{ "status": 409, "error": "Conflict", "message": "Já existe um prazo com este processo, descrição e data." }
```

---

## Observabilidade (logs)

Os logs são escritos em **JSON estruturado (padrão ECS)** em `backend/logs/prazos.json`,
com um **`requestId`** por requisição (propagado via MDC e devolvido no header `X-Request-Id`).
Isso permite rastrear toda a jornada de uma requisição. Exemplo:

```json
{ "@timestamp": "...", "log": {"level":"INFO"}, "message": "Prazo criado id=1 ...", "requestId": "0fff2b34-..." }
```

No console, os logs ficam em formato legível para facilitar o desenvolvimento.

---

## Estrutura do projeto

```
attus-prazos/
├── backend/            # API Spring Boot
│   └── src/main/java/com/attus/prazos/
│       ├── domain/         # Entidade Prazo, StatusPrazo
│       ├── repository/     # PrazoRepository (Spring Data JPA)
│       ├── service/        # PrazoService (regra de negócio)
│       └── web/            # Controller, DTOs, ExceptionHandler, RequestIdFilter
├── frontend/           # App React + TypeScript (Vite)
│   └── src/
│       ├── components/     # PrazoForm, PrazoList
│       ├── api.ts          # Cliente HTTP
│       └── types.ts        # Tipos espelhando o contrato da API
├── docs/
│   ├── INCIDENT_ANALYSIS.md  # Parte 2 — análise de incidente
│   └── TECH_NOTES.md         # Decisões técnicas, trade-offs e melhorias
└── docker-compose.yml
```

---

## Documentação adicional

- 📄 [Análise de incidente (Parte 2)](docs/INCIDENT_ANALYSIS.md)
- 📄 [Nota técnica — decisões e trade-offs](docs/TECH_NOTES.md)
