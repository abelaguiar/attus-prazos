import { useEffect, useState } from 'react';
import './App.css';
import { cumprirPrazo, listarPrazos } from './api';
import { PrazoForm } from './components/PrazoForm';
import { PrazoList } from './components/PrazoList';
import type { Prazo } from './types';

export default function App() {
  const [prazos, setPrazos] = useState<Prazo[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    listarPrazos()
      .then(setPrazos)
      .catch(() => setErro('Não foi possível carregar os prazos. O back-end está rodando em :8080?'))
      .finally(() => setCarregando(false));
  }, []);

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

  return (
    <main className="container">
      <header>
        <h1>Monitor de Prazos Processuais</h1>
        <p className="subtitulo">Cadastre e acompanhe os prazos dos processos.</p>
      </header>

      <PrazoForm onCriado={handleCriado} />

      <section>
        <h2>Prazos</h2>
        {carregando && <p>Carregando...</p>}
        {erro && <p className="erro-geral">{erro}</p>}
        {!carregando && !erro && <PrazoList prazos={prazos} onCumprir={handleCumprir} />}
      </section>
    </main>
  );
}
