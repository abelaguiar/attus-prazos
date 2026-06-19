# Análise de incidente — lost update na edição de prazos

## Resumo

Dois procuradores editando o mesmo prazo ao mesmo tempo causavam lost update: a alteração de um
era sobrescrita pela do outro, que tinha carregado uma versão antiga do registro. Não dava erro
nenhum; a edição perdida só sumia.

A correção introduz trava otimista. A entidade `Prazo` ganhou `@Version`, e a edição passou a
exigir a versão que o cliente carregou. Se essa versão estiver defasada, a operação é recusada
com HTTP 409 em vez de gravar por cima.

## Sintoma

O relato era do tipo "editei a data de um prazo, salvei, e pouco depois minha alteração tinha
voltado". A alteração de um usuário se perdia sem aviso. Num sistema de prazos processuais isso
não é pouca coisa: significa trabalhar com uma data errada e, no limite, perder prazo. Acontecia
sempre que dois usuários editavam o mesmo prazo numa janela de tempo próxima.

## O que os logs mostraram

Reproduzindo: dois usuários carregam o prazo na versão 0. O usuário A edita para "Apelação"
(o registro vai para a versão 1) e o usuário B, ainda com a versão 0 na tela, edita para
"Embargos".

```
12:37:42.007  [INFO]  Prazo atualizado id=1 versaoCliente=0 versaoAtual=0
12:37:42.034  [INFO]  Prazo atualizado id=1 versaoCliente=0 versaoAtual=1
```

A segunda linha é a pista: o cliente B mandou `versaoCliente=0`, mas o registro já estava em
`versaoAtual=1`. Ou seja, B editou em cima de uma versão velha e o sistema aceitou assim mesmo.
As duas requisições entraram como INFO, sem nenhum sinal de erro. No fim, a "Apelação" do A
desaparece e fica a "Embargos" do B.

## Causa-raiz

Faltava controle de concorrência. A edição era um read-modify-write que não verificava se o
registro tinha mudado entre o momento em que o cliente leu e o momento em que ele gravou. Dois
clientes que leem a mesma versão e gravam em sequência: o segundo ignora o que o primeiro fez.
É o caso clássico de lost update.

## Correção

1. `@Version` na entidade `Prazo`. O JPA passa a versionar cada linha e a usar a versão na
   cláusula do update (`... WHERE id = ? AND version = ?`).
2. A edição passa a exigir a versão do cliente. O `PUT /prazos/{id}` recebe o campo `version`
   (o mesmo que veio no `GET`); o serviço compara com a versão atual e, se for diferente, lança
   `ConflitoDeVersaoException`, que vira 409, sem gravar.
3. A corrida fina no banco também fica coberta: se duas requisições passarem pela verificação
   ao mesmo tempo, o `@Version` faz o update de uma delas afetar 0 linhas e o JPA lança
   `ObjectOptimisticLockingFailureException`, que também é mapeada para 409.

As duas camadas têm papéis diferentes. A checagem explícita da `version` resolve o caso comum,
que é o read-modify-write entre requisições HTTP separadas. O `@Version` resolve a corrida que
acontece dentro do banco. Juntas, fecham a brecha.

Depois da correção, a mesma sequência produz:

```
[WARN]  Conflito de versão id=1 versaoCliente=0 versaoAtual=1
```

E a resposta da edição com versão defasada:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "O prazo 1 foi modificado por outra operação. Recarregue e tente novamente.",
  "path": "/prazos/1"
}
```

A alteração do primeiro usuário é mantida. Os testes que cobrem isso são o
`deveRejeitarAtualizacaoComVersaoDesatualizadaCom409` e o `deveAtualizarPrazoComVersaoCorreta`,
no `PrazoControllerTest`.

## Prevenção

O que já entrou com a correção:

- `@Version` no banco/ORM, cobrindo a corrida de escrita.
- A API exigindo a `version` na edição e devolvendo 409 no conflito.
- Teste do cenário de versão desatualizada.

O que ainda fica como sugestão, do lado do front:

- Usar a `version` que a API devolve e, ao receber 409, recarregar e avisar o usuário de que o
  prazo mudou, em vez de só mostrar um erro genérico.

## Ideias para depois

- ETag e `If-Match`: expor a versão como `ETag` e aceitar `If-Match` no `PUT`, padronizando a
  concorrência otimista pelos cabeçalhos HTTP.
- Auditoria: registrar quem alterou cada prazo e quando, o que ajuda a investigar conflitos.
- Merge assistido: em vez de só recusar, mostrar a diferença entre a versão do usuário e a
  atual e deixar ele decidir.
