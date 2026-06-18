# Análise de Incidente — Prazos duplicados / erros recorrentes em `POST /prazos`

## Resumo (TL;DR)

Procuradores relataram **prazos duplicados** na lista de monitoramento. A investigação
nos logs mostrou que o mesmo prazo (processo + descrição + data) era gravado **duas vezes**
em requisições quase simultâneas — assinatura clássica de **duplo-clique / retry** do
cliente. O sistema não tinha nenhuma garantia de unicidade, então aceitava as duplicatas
silenciosamente (HTTP 201), corrompendo os dados sem gerar erro.

A correção (esta PR) adiciona uma **constraint de unicidade no banco** e passa a tratar a
violação como **HTTP 409 Conflict** (e não 500), registrando o evento como **WARN**.

---

## 1. Sintoma

- **Reportado pelo usuário:** "o mesmo prazo aparece duas vezes na lista".
- **Impacto:** retrabalho (procurador trata o mesmo prazo duas vezes), risco de ruído em
  contagem de prazos e em futuros alertas de vencimento.
- **Frequência:** recorrente, sob duplo-clique no botão de cadastro ou reenvio da requisição
  (retry de rede / refresh).

## 2. Evidência nos logs (ANTES)

Reproduzindo dois `POST /prazos` idênticos, o log estruturado (`logs/prazos.json`) registrou:

```
2026-06-18T20:22:15.485Z  [INFO]  requestId=d7fd4c5a  Prazo criado id=8 numeroProcesso=0009999-11.2026.8.26.0100 dataPrazo=2026-09-10
2026-06-18T20:22:15.497Z  [INFO]  requestId=413cf9fc  Prazo criado id=9 numeroProcesso=0009999-11.2026.8.26.0100 dataPrazo=2026-09-10
```

Leitura dos logs (diagnóstico):

| Pista no log | Conclusão |
|---|---|
| Dois `id` distintos (8 e 9), mesmo processo/descrição/data | É **duplicata**, não dois prazos legítimos |
| `requestId` diferente em cada linha (`d7fd4c5a` vs `413cf9fc`) | São **duas requisições HTTP distintas** — origem no cliente, não loop interno |
| Timestamps a **~12 ms** de distância | Praticamente simultâneas → **duplo-submit** |
| Ambas `INFO` / HTTP 201 | **Corrupção silenciosa**: o sistema acha que está tudo certo |

## 3. Causa-raiz

1. **Sem garantia de unicidade.** Não existia constraint no banco nem verificação que
   impedisse gravar o mesmo prazo (mesmo `numero_processo` + `descricao` + `data_prazo`)
   mais de uma vez.
2. **Operação não-idempotente.** Dois `POST` idênticos produzem dois registros. Sob
   duplo-clique/retry — cenário comum em qualquer formulário web — isso vira duplicata.

> Observação importante: validar a unicidade **apenas na aplicação** (um `SELECT` antes do
> `INSERT`) **não resolve** sob concorrência — duas requisições simultâneas passam as duas
> pela checagem antes de qualquer `INSERT` (condição de corrida). A única garantia real é a
> **constraint no banco**.

## 4. Correção aplicada (nesta PR)

A correção tem duas partes, e ambas são necessárias:

1. **Integridade — constraint de unicidade no banco** (`Prazo`):
   `UNIQUE (numero_processo, descricao, data_prazo)`. Garante, no nível mais baixo e à prova
   de concorrência, que a duplicata não é gravada.

2. **Semântica de erro — tratar o conflito como 409, não 500.** Com a constraint, um segundo
   `POST` idêntico faz o JPA lançar `DataIntegrityViolationException`. Sem tratamento, ela
   cairia no handler genérico e viraria **HTTP 500 + log ERROR com stacktrace** — ou seja,
   trocaríamos "corrupção silenciosa" por "erro 500 recorrente", parecendo bug do servidor.
   Por isso adicionamos um handler dedicado que devolve **HTTP 409 Conflict** com mensagem
   clara e registra **WARN** (é um problema esperado do cliente, não uma falha do servidor).

### Evidência nos logs (DEPOIS)

```
2026-06-18T20:38:35.452Z  [WARN]  requestId=71ca2b5a  Conflito de integridade em /prazos: prazo duplicado
```

Resposta HTTP do segundo `POST` idêntico:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Já existe um prazo com este processo, descrição e data.",
  "path": "/prazos"
}
```

Cobertura de teste: `PrazoControllerTest.deveRejeitarPrazoDuplicadoCom409`.

## 5. Prevenção (defesa em camadas)

| Camada | Medida | Situação |
|---|---|---|
| Banco | Constraint `UNIQUE` (garantia final, à prova de concorrência) | ✅ nesta PR |
| API | Mapear conflito → 409 + log WARN (sem falso 5xx) | ✅ nesta PR |
| Testes | Caso de duplicata cobrindo o 409 | ✅ nesta PR |
| Front-end | Desabilitar o botão durante o envio (evita duplo-clique) | ✅ já existente (`PrazoForm`, estado `enviando`) |
| Observabilidade | Alerta sobre taxa de 5xx e sobre pico de 409 | ⏳ sugerido (futuro) |

## 6. Aplicação segura em produção (rollout da constraint)

> ⚠️ **Atenção:** como o incidente indica que **já existem duplicatas gravadas**, criar a
> constraint diretamente (via `ddl-auto=update` ou um `ALTER TABLE` simples) **falha**: o
> banco não consegue adicionar um índice único sobre dados que já violam a regra. Por isso,
> em produção a constraint **não** deve subir pelo `ddl-auto` — ela exige um rollout em etapas:

1. **Saneamento dos dados existentes** — identificar e mesclar/remover as duplicatas antes de
   aplicar a constraint. Exemplo (mantém a linha de menor `id` de cada grupo duplicado):
   ```sql
   DELETE FROM prazo p
   USING prazo q
   WHERE p.numero_processo = q.numero_processo
     AND p.descricao       = q.descricao
     AND p.data_prazo      = q.data_prazo
     AND p.id > q.id;
   ```
2. **Migração versionada** — aplicar a constraint via **Flyway/Liquibase** (não por `ddl-auto`),
   em SQL auditável e reversível:
   ```sql
   ALTER TABLE prazo
     ADD CONSTRAINT uk_prazo_processo_descricao_data
     UNIQUE (numero_processo, descricao, data_prazo);
   ```
3. **Ordem de implantação** — rodar (1) e (2) **antes** de subir a nova versão da aplicação,
   garantindo que o código que retorna 409 só entre em operação com o banco já consistente.

> No projeto, por simplicidade, o schema é gerado pelo Hibernate (`ddl-auto=update`) sobre um
> H2 em memória que sempre nasce vazio — então o problema acima não se manifesta em dev/teste.
> O plano acima é o que seria aplicado num banco real com histórico.

## 7. Melhorias futuras

- **Idempotency key:** aceitar um header `Idempotency-Key` no `POST`, de modo que um retry
  com a mesma chave retorne o recurso já criado (200) em vez de 409 — UX mais suave para
  retries legítimos de rede.
- **Migrações versionadas (Flyway/Liquibase):** adotar como padrão do projeto (não só para
  esta constraint), tornando todo o schema versionado e auditável.
- **Alertas automáticos:** disparar alerta quando a taxa de 5xx ou de 409 ultrapassar um
  limiar, para detectar o problema antes do usuário reportar.
