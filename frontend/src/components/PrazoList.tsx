import type { Prazo } from '../types';

interface Props {
  prazos: Prazo[];
  onCumprir: (id: number) => void;
}

function rotuloSituacao(prazo: Prazo): { texto: string; classe: string } {
  if (prazo.status === 'CUMPRIDO') {
    return { texto: 'Cumprido', classe: 'cumprido' };
  }
  if (prazo.vencido) {
    return { texto: 'Vencido', classe: 'vencido' };
  }
  return { texto: 'Pendente', classe: 'pendente' };
}

function formatarData(iso: string): string {
  return new Date(`${iso}T00:00:00`).toLocaleDateString('pt-BR');
}

export function PrazoList({ prazos, onCumprir }: Props) {
  if (prazos.length === 0) {
    return <p className="vazio">Nenhum prazo cadastrado ainda.</p>;
  }

  return (
    <table className="prazo-table">
      <thead>
        <tr>
          <th>Processo</th>
          <th>Descrição</th>
          <th>Prazo</th>
          <th>Situação</th>
          <th>Ação</th>
        </tr>
      </thead>
      <tbody>
        {prazos.map((prazo) => {
          const situacao = rotuloSituacao(prazo);
          return (
            <tr key={prazo.id}>
              <td>{prazo.numeroProcesso}</td>
              <td>{prazo.descricao}</td>
              <td>{formatarData(prazo.dataPrazo)}</td>
              <td>
                <span className={`badge ${situacao.classe}`}>{situacao.texto}</span>
              </td>
              <td>
                {prazo.status === 'PENDENTE' && (
                  <button type="button" onClick={() => onCumprir(prazo.id)}>
                    Marcar cumprido
                  </button>
                )}
              </td>
            </tr>
          );
        })}
      </tbody>
    </table>
  );
}
