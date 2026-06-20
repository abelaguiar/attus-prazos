# Nota técnica

Algumas decisões que tomei no projeto e o motivo de cada uma.

## Um modelo só (Prazo)

Modelei apenas `Prazo`, sem relacionamentos. O objetivo do teste é mostrar um fluxo completo
com qualidade, não um modelo de dados grande, e uma entidade só já dá pra exercitar regra de
negócio, validação e estado derivado mantendo o código fácil de ler e testar. Fica menos
"vistoso", mas evita complexidade que o problema não pede.

## "Vencido" é calculado

Um prazo está vencido quando a data já passou e ele ainda está `PENDENTE`. Em vez de guardar
isso numa coluna, calculo na hora da leitura (`Prazo.isVencido()`). Coluna armazenada precisaria
de um job atualizando o status todo dia, e é fácil esse status ficar desatualizado. Calculando,
o problema simplesmente não existe.

## DTO em vez da entidade na API

A API recebe e devolve DTOs (`CriarPrazoRequest`, `PrazoResponse`), não a entidade. Isso separa
o contrato HTTP do modelo de banco: dá pra mudar o mapeamento sem quebrar a API e controlar
exatamente o que entra e sai.

## Português no domínio, inglês na infra

Os nomes de negócio ficam em português (`Prazo`, `numeroProcesso`, `marcarComoCumprido`) porque
essa é a língua do domínio jurídico. Os termos técnicos seguem a convenção do Spring em inglês
(`Repository`, `Service`, `findAll`). É a ideia de linguagem ubíqua: o código fala a língua de
quem entende do negócio.

## Tratamento de erro num lugar só

Um `@RestControllerAdvice` global converte as exceções em respostas 400/404/409/500
consistentes, o que mantém os controllers limpos. O tratamento de violação de integridade é
específico: só vira 409 quando é a constraint de duplicidade; qualquer outra violação cai em 500
com log de erro, para não esconder um problema real atrás de um status enganoso.

## Logs em JSON com requestId

Os logs saem em JSON (padrão ECS) com um `requestId` por requisição (via MDC, devolvido no
header `X-Request-Id`). Isso deixa o log pesquisável e foi o que permitiu fazer a análise da
Parte 2. No console o formato fica legível; no arquivo, estruturado.

## Validação no front e no back

O front valida para dar resposta rápida ao usuário, mas a validação que conta é a do back (Bean
Validation). O servidor nunca confia no que vem do cliente.

## Unicidade garantida no banco

A regra de "não pode haver prazo duplicado" é uma constraint `UNIQUE` no banco, não um `if` na
aplicação. Sob concorrência, checar com um `SELECT` antes do `INSERT` tem condição de corrida:
duas requisições passam pela checagem antes de qualquer uma gravar. Só a constraint garante a
regra de verdade.

Como `descricao` aceita texto livre maior, ela não entra diretamente no índice único. A entidade
mantém um `descricao_hash` SHA-256 e a constraint usa `(numero_processo, descricao_hash,
data_prazo)`. Assim preservamos a regra de duplicidade sem indexar o `text` inteiro no
PostgreSQL. O número do processo também é normalizado para dígitos antes de persistir, evitando
duplicidade entre valores com e sem máscara.

## Concorrência otimista na edição

A edição usa trava otimista. A entidade tem `@Version` e o `PUT` exige a `version` que o cliente
carregou; se estiver defasada, a operação é recusada com 409 em vez de sobrescrever. Escolhi
otimista porque conflito de edição é raro — travar a linha (pessimista) penalizaria o caso
comum, que é não ter conflito nenhum.

São duas camadas com papéis diferentes: a checagem explícita da `version` cobre o caso de dois
usuários editando em requisições separadas (o read-modify-write), e o `@Version` cobre a corrida
fina dentro do banco, que vira `ObjectOptimisticLockingFailureException` e também é mapeada para
409. Os detalhes estão em `INCIDENT_ANALYSIS_LOST_UPDATE.md`.

## H2 em memória

Em dev e nos testes uso H2 em memória, então ninguém precisa instalar banco para rodar o
projeto. Os dados somem ao reiniciar, o que é aceitável para um teste. Trocar por PostgreSQL é
só configuração de datasource (é o que o profile `docker` faz).

## O que faria depois

- Migrações versionadas (Flyway ou Liquibase). Hoje o schema é gerado pelo Hibernate
  (`ddl-auto`) e o profile Docker tem um script idempotente para ajustes do PostgreSQL; em
  produção isso precisa ser versionado e auditável.
- Idempotency key no `POST /prazos`, para um retry de rede devolver o recurso já criado em vez
  de 409.
- ETag e `If-Match` na edição, levando a concorrência otimista para os cabeçalhos HTTP em vez de
  um campo no corpo.
- OpenAPI/Swagger para documentação interativa da API.
- Paginação e filtros no `GET /prazos`.
- Autenticação e auditoria de quem cumpriu cada prazo.
- Cálculo de prazo em dias úteis, considerando feriados forenses.
- Testes end-to-end no front (Playwright, por exemplo).
