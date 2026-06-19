import { useEffect, useState } from 'react';
import './App.css';
import { cumprirPrazo, listarPrazos } from './api';
import { PrazoForm } from './components/PrazoForm';
import { PrazoEditForm } from './components/PrazoEditForm';
import { PrazoList } from './components/PrazoList';
import type { Prazo } from './types';

export default function App() {
  const [prazos, setPrazos] = useState<Prazo[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [prazoEmEdicao, setPrazoEmEdicao] = useState<Prazo | null>(null);

  useEffect(() => {
    listarPrazos()
      .then(setPrazos)
      .catch(() => setErro('Não foi possível carregar os prazos. O back-end está rodando em :8080?'))
      .finally(() => setCarregando(false));
  }, []);

  async function recarregarPrazos() {
    try {
      setPrazos(await listarPrazos());
    } catch {
      setErro('Não foi possível recarregar os prazos.');
    }
  }

  function handleCriado(novo: Prazo) {
    setPrazos((atual) => [...atual, novo]);
  }

  async function handleCumprir(id: number) {
    try {
      const atualizado = await cumprirPrazo(id);
      setPrazos((atual) => atual.map((p) => (p.id === id ? atualizado : p)));
    } catch {
      setErro('Não foi possível marcar o prazo como cumprido.');
    }
  }

  function handleEditar(prazo: Prazo) {
    setErro(null);
    setPrazoEmEdicao(prazo);
  }

  function handleSalvo(atualizado: Prazo) {
    setPrazos((atual) => atual.map((p) => (p.id === atualizado.id ? atualizado : p)));
    setPrazoEmEdicao(null);
  }

  async function handleConflito() {
    setPrazoEmEdicao(null);
    setErro('Este prazo foi alterado por outra pessoa. A lista foi atualizada — confira e tente de novo.');
    await recarregarPrazos();
  }

  return (
    <main className="container">
      <header>
        <h1>Monitor de Prazos Processuais</h1>
        <p className="subtitulo">Cadastre e acompanhe os prazos dos processos.</p>
      </header>

      {prazoEmEdicao ? (
        <PrazoEditForm
          key={prazoEmEdicao.id}
          prazo={prazoEmEdicao}
          onSalvo={handleSalvo}
          onConflito={handleConflito}
          onCancelar={() => setPrazoEmEdicao(null)}
        />
      ) : (
        <PrazoForm onCriado={handleCriado} />
      )}

      <section>
        <h2>Prazos</h2>
        {carregando && <p>Carregando...</p>}
        {erro && <p className="erro-geral">{erro}</p>}
        {!carregando && !(erro && prazos.length === 0) && (
          <PrazoList prazos={prazos} onCumprir={handleCumprir} onEditar={handleEditar} />
        )}
      </section>
    </main>
  );
}
