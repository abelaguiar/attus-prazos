# Front-end — Monitor de Prazos

Interface em React 19 + TypeScript (Vite) para cadastrar, editar, marcar como cumprido e
acompanhar prazos processuais. Consome a API em `http://localhost:8080`.

## Rodar

```bash
npm install
npm run dev      # http://localhost:5173
```

Outros scripts: `npm run build` (type-check com `tsc` + bundle de produção), `npm run lint`
(ESLint) e `npm run preview` (servir o build localmente).

## Estrutura

- `components/` — `PrazoForm` (cadastro), `PrazoEditForm` (edição), `PrazoList` (listagem) e
  `icons`
- `mascaras.ts` — máscara CNJ do número do processo (formata enquanto se digita)
- `api.ts` — cliente HTTP e tratamento de erro da API
- `types.ts` — tipos que espelham o contrato da API

## Notas

- O número do processo é mascarado no padrão CNJ e validado em 20 dígitos antes de enviar; a
  validação definitiva é a do back-end.
- A descrição é um `textarea` (texto livre, até 2000 caracteres).
- A edição envia a `version` lida (controle de concorrência otimista); um `409` indica que o
  prazo mudou e a tela precisa ser recarregada.
