import { useCallback, useEffect, useState } from 'react';
import './App.css';
import { cumprirPrazo, listarPrazos } from './api';
import { PrazoForm } from './components/PrazoForm';
import { PrazoEditForm } from './components/PrazoEditForm';
import { PrazoList } from './components/PrazoList';
import { IconChevronLeft, IconChevronRight } from './components/icons';
import type { Prazo } from './types';

const TAMANHO_PAGINA = 10;

export default function App() {
  const [prazos, setPrazos] = useState<Prazo[]>([]);
  const [pagina, setPagina] = useState(0);
  const [totalPaginas, setTotalPaginas] = useState(0);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [prazoEmEdicao, setPrazoEmEdicao] = useState<Prazo | null>(null);

  const carregarPagina = useCallback(async (p: number) => {
    setCarregando(true);
    try {
      const resultado = await listarPrazos(p, TAMANHO_PAGINA);
      setPrazos(resultado.content);
      setPagina(resultado.page);
      setTotalPaginas(resultado.totalPages);
      setErro(null);
    } catch {
      setErro('Não foi possível carregar os prazos. O back-end está rodando em :8080?');
    } finally {
      setCarregando(false);
    }
  }, []);

  useEffect(() => {
    carregarPagina(0);
  }, [carregarPagina]);

  function handleCriado() {
    carregarPagina(pagina);
  }

  async function handleCumprir(id: number) {
    try {
      await cumprirPrazo(id);
      await carregarPagina(pagina);
    } catch {
      setErro('Não foi possível marcar o prazo como cumprido.');
    }
  }

  function handleEditar(prazo: Prazo) {
    setErro(null);
    setPrazoEmEdicao(prazo);
  }

  function handleSalvo() {
    setPrazoEmEdicao(null);
    carregarPagina(pagina);
  }

  async function handleConflito() {
    setPrazoEmEdicao(null);
    // recarrega ANTES de exibir o aviso, pois carregarPagina limpa o erro no sucesso
    await carregarPagina(pagina);
    setErro('Este prazo foi alterado por outra pessoa. A lista foi atualizada — confira e tente de novo.');
  }

  return (
    <>
      <header className="app-header">
        <div className="app-header__inner">
          <h1>Monitor de Prazos Processuais</h1>
          <p className="subtitulo">Cadastre e acompanhe os prazos dos processos.</p>
        </div>
      </header>

      <main className="container">
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
            <>
              <PrazoList prazos={prazos} onCumprir={handleCumprir} onEditar={handleEditar} />
              {totalPaginas > 1 && (
                <div className="paginacao">
                  <button
                    type="button"
                    className="btn btn-ghost btn-sm"
                    disabled={pagina === 0}
                    onClick={() => carregarPagina(pagina - 1)}
                  >
                    <IconChevronLeft size={14} />
                    Anterior
                  </button>
                  <span>
                    Página {pagina + 1} de {totalPaginas}
                  </span>
                  <button
                    type="button"
                    className="btn btn-ghost btn-sm"
                    disabled={pagina >= totalPaginas - 1}
                    onClick={() => carregarPagina(pagina + 1)}
                  >
                    Próxima
                    <IconChevronRight size={14} />
                  </button>
                </div>
              )}
            </>
          )}
        </section>
      </main>
    </>
  );
}
