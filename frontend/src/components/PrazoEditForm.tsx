import { useState, type FormEvent } from 'react';
import { ApiException, atualizarPrazo } from '../api';
import type { Prazo } from '../types';

interface Props {
  prazo: Prazo;
  onSalvo: (prazo: Prazo) => void;
  onConflito: () => void;
  onCancelar: () => void;
}

export function PrazoEditForm({ prazo, onSalvo, onConflito, onCancelar }: Props) {
  const [descricao, setDescricao] = useState(prazo.descricao);
  const [dataPrazo, setDataPrazo] = useState(prazo.dataPrazo);
  const [erros, setErros] = useState<Record<string, string>>({});
  const [enviando, setEnviando] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setEnviando(true);
    setErros({});
    try {
      const atualizado = await atualizarPrazo(prazo.id, {
        descricao,
        dataPrazo,
        version: prazo.version,
      });
      onSalvo(atualizado);
    } catch (err) {
      if (err instanceof ApiException) {
        if (err.apiError.status === 409) {
          onConflito();
          return;
        }
        const mapa: Record<string, string> = {};
        for (const fe of err.apiError.fieldErrors) {
          mapa[fe.field] = fe.message;
        }
        setErros(mapa);
      } else {
        setErros({ geral: 'Não foi possível salvar. O back-end está rodando?' });
      }
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form className="prazo-form" onSubmit={handleSubmit} noValidate>
      <h2>Editar prazo — {prazo.numeroProcesso}</h2>

      <label>
        Descrição
        <input
          type="text"
          value={descricao}
          onChange={(e) => setDescricao(e.target.value)}
        />
        {erros.descricao && <span className="erro-campo">{erros.descricao}</span>}
      </label>

      <label>
        Data do prazo
        <input
          type="date"
          value={dataPrazo}
          onChange={(e) => setDataPrazo(e.target.value)}
        />
        {erros.dataPrazo && <span className="erro-campo">{erros.dataPrazo}</span>}
      </label>

      {erros.geral && <p className="erro-geral">{erros.geral}</p>}

      <div className="form-acoes">
        <button type="submit" disabled={enviando}>
          {enviando ? 'Salvando...' : 'Salvar alterações'}
        </button>
        <button type="button" className="secundario" onClick={onCancelar} disabled={enviando}>
          Cancelar
        </button>
      </div>
    </form>
  );
}
