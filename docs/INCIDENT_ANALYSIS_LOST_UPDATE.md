# Análise de Incidente — Lost update na edição de prazos (concorrência)

## Resumo (TL;DR)

Dois procuradores editando o **mesmo prazo** ao mesmo tempo causavam **lost update**:
a alteração de um era **sobrescrita silenciosamente** pela do outro (que havia carregado uma
versão antiga do registro). Nenhum erro era gerado — a edição "perdida" simplesmente sumia.

A correção (esta PR) introduz **trava otimista**: a entidade `Prazo` ganha `@Version`, e a
edição passa a exigir a **versão** que o cliente carregou. Se a versão estiver desatualizada,
a operação é rejeitada com **HTTP 409 Conflict** em vez de gravar por cima.

---

## 1. Sintoma

- **Reportado:** "editei a data de um prazo, salvei, e pouco depois minha alteração tinha
  voltado ao valor antigo / virado outra coisa."
- **Impacto:** alteração de um usuário perdida sem aviso. Num domínio de prazos processuais,
  isso pode significar trabalhar com uma data errada — risco real de perder prazo.
- **Frequência:** recorrente quando dois usuários editam o mesmo prazo em janela próxima.

## 2. Evidência nos logs (ANTES)

Reproduzindo: dois usuários carregam o prazo na **versão 0**; A edita para "Apelação"
(vira versão 1); B, ainda com a versão 0 em tela, edita para "Embargos".

```
12:37:42.007  [INFO]  Prazo atualizado id=1 versaoCliente=0 versaoAtual=0
12:37:42.034  [INFO]  Prazo atualizado id=1 versaoCliente=0 versaoAtual=1
```

Leitura dos logs (diagnóstico):

| Pista no log | Conclusão |
|---|---|
| 2ª linha: `versaoCliente=0` mas `versaoAtual=1` | O cliente B editou baseado numa versão **desatualizada** (a 0), enquanto o registro já estava na 1 |
| Ambas `INFO`, ambas aceitas | A escrita obsoleta foi **gravada por cima** sem aviso — *lost update* |
| Resultado | A alteração "Apelação" (de A) **desaparece**; fica "Embargos" (de B) |

## 3. Causa-raiz

Falta de **controle de concorrência otimista**. A operação de edição era um
*read-modify-write* sem verificar se o registro havia mudado entre o "read" do cliente e o
"write". Dois clientes que leem a mesma versão e escrevem em sequência: o segundo write
ignora o que o primeiro fez. É o **lost update** clássico.

## 4. Correção aplicada (nesta PR)

1. **`@Version` na entidade `Prazo`** — o JPA passa a versionar cada linha e a usar a versão
   na cláusula do `UPDATE` (`... WHERE id = ? AND version = ?`).
2. **Edição exige a versão do cliente** — `PUT /prazos/{id}` recebe o campo `version` (que o
   cliente obteve no `GET`). O serviço compara com a versão atual; se diferente, lança
   `ConflitoDeVersaoException` → **HTTP 409**, sem gravar.
3. **Proteção também para corrida real no banco** — se duas requisições passarem pela
   verificação simultaneamente, o `@Version` faz o `UPDATE` de uma delas afetar 0 linhas →
   `ObjectOptimisticLockingFailureException`, também mapeada para **409**.

> Por que as duas camadas? A verificação explícita da `version` resolve o caso comum
> (read-modify-write entre **requisições HTTP separadas** — *optimistic offline lock*). O
> `@Version` cobre a corrida fina dentro do banco. Juntas, fecham a janela de lost update.

### Evidência nos logs (DEPOIS)

```
[WARN]  Conflito de versão id=1 versaoCliente=0 versaoAtual=1
```

Resposta HTTP da edição com versão desatualizada:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "O prazo 1 foi modificado por outra operação. Recarregue e tente novamente.",
  "path": "/prazos/1"
}
```

A alteração do primeiro usuário é **preservada**. Cobertura de teste:
`PrazoControllerTest.deveRejeitarAtualizacaoComVersaoDesatualizadaCom409` e
`deveAtualizarPrazoComVersaoCorreta`.

## 5. Prevenção (defesa em camadas)

| Camada | Medida | Situação |
|---|---|---|
| Banco / ORM | `@Version` (lock otimista, cobre corrida no banco) | ✅ nesta PR |
| API | Exige `version` na edição; conflito → 409 | ✅ nesta PR |
| Testes | Cenário de versão desatualizada → 409 | ✅ nesta PR |
| Front-end | Usar o `version` retornado e, ao receber 409, avisar o usuário e recarregar | ⏳ sugerido |
| UX | Mostrar "este prazo foi alterado por outra pessoa; recarregue" | ⏳ sugerido |

## 6. Melhorias futuras

- **ETag / If-Match:** expor a versão como `ETag` e aceitar `If-Match` no `PUT`, padronizando
  a concorrência otimista via cabeçalhos HTTP (em vez de um campo no corpo).
- **Auditoria:** registrar quem alterou cada prazo e quando, para rastrear conflitos.
- **Merge assistido:** em vez de só rejeitar, mostrar as diferenças entre a versão do usuário
  e a atual, deixando-o decidir.
