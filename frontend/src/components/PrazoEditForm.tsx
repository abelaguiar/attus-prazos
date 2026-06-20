import { useState, type FormEvent } from 'react';
import { ApiException, atualizarPrazo, mapearErrosDeCampo } from '../api';
import type { Prazo } from '../types';
import { IconSave, IconX } from './icons';

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

  function validarLocal(): Record<string, string> {
    const novos: Record<string, string> = {};
    if (!descricao.trim()) novos.descricao = 'Informe a descrição';
    if (!dataPrazo) novos.dataPrazo = 'Informe a data do prazo';
    return novos;
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();

    const errosLocais = validarLocal();
    if (Object.keys(errosLocais).length > 0) {
      setErros(errosLocais);
      return;
    }

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
      // 409 = o prazo mudou no servidor desde que abrimos a edição; deixa o pai recarregar.
      if (err instanceof ApiException && err.apiError.status === 409) {
        onConflito();
        return;
      }
      setErros(mapearErrosDeCampo(err));
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form className="prazo-form" onSubmit={handleSubmit} noValidate>
      <h2>Editar prazo — {prazo.numeroProcesso}</h2>

      <label>
        Descrição
        <textarea
          value={descricao}
          onChange={(e) => setDescricao(e.target.value)}
          rows={3}
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
        <button type="submit" className="btn btn-primary" disabled={enviando}>
          <IconSave />
          {enviando ? 'Salvando...' : 'Salvar alterações'}
        </button>
        <button type="button" className="btn btn-ghost" onClick={onCancelar} disabled={enviando}>
          <IconX />
          Cancelar
        </button>
      </div>
    </form>
  );
}
