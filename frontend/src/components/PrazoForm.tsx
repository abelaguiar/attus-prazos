import { useState, type FormEvent } from 'react';
import { criarPrazo, mapearErrosDeCampo } from '../api';
import { mascaraProcesso } from '../mascaras';
import type { Prazo } from '../types';
import { IconPlus } from './icons';

interface Props {
  onCriado: (prazo: Prazo) => void;
}

export function PrazoForm({ onCriado }: Props) {
  const [numeroProcesso, setNumeroProcesso] = useState('');
  const [descricao, setDescricao] = useState('');
  const [dataPrazo, setDataPrazo] = useState('');
  const [erros, setErros] = useState<Record<string, string>>({});
  const [enviando, setEnviando] = useState(false);

  function validarLocal(): Record<string, string> {
    const novos: Record<string, string> = {};
    const digitos = numeroProcesso.replace(/\D/g, '');
    if (!digitos) novos.numeroProcesso = 'Informe o número do processo';
    else if (digitos.length !== 20)
      novos.numeroProcesso = 'Número do processo incompleto (20 dígitos)';
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
      const criado = await criarPrazo({ numeroProcesso, descricao, dataPrazo });
      onCriado(criado);
      setNumeroProcesso('');
      setDescricao('');
      setDataPrazo('');
    } catch (err) {
      setErros(mapearErrosDeCampo(err));
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form className="prazo-form" onSubmit={handleSubmit} noValidate>
      <h2>Novo prazo</h2>

      <label>
        Número do processo
        <input
          type="text"
          inputMode="numeric"
          value={numeroProcesso}
          onChange={(e) => setNumeroProcesso(mascaraProcesso(e.target.value))}
          placeholder="0001234-56.2026.8.26.0100"
        />
        {erros.numeroProcesso && <span className="erro-campo">{erros.numeroProcesso}</span>}
      </label>

      <label>
        Descrição
        <textarea
          value={descricao}
          onChange={(e) => setDescricao(e.target.value)}
          placeholder="Contestação"
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

      <button type="submit" className="btn btn-primary" disabled={enviando}>
        <IconPlus />
        {enviando ? 'Salvando...' : 'Cadastrar prazo'}
      </button>
    </form>
  );
}
