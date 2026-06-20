import { useState, type FormEvent } from 'react';
import { ApiException, login, registrar } from '../api';

interface Props {
  onAutenticado: () => void;
}

type Modo = 'login' | 'cadastro';

export function AuthForm({ onAutenticado }: Props) {
  const [modo, setModo] = useState<Modo>('login');
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [erros, setErros] = useState<Record<string, string>>({});
  const [enviando, setEnviando] = useState(false);

  const ehCadastro = modo === 'cadastro';

  function trocarModo(novo: Modo) {
    setModo(novo);
    setErros({});
    setSenha('');
  }

  function validarLocal(): Record<string, string> {
    const novos: Record<string, string> = {};
    if (ehCadastro && !nome.trim()) novos.nome = 'Informe seu nome';
    if (!email.trim()) novos.email = 'Informe o e-mail';
    if (!senha) novos.senha = 'Informe a senha';
    else if (ehCadastro && senha.length < 8)
      novos.senha = 'A senha deve ter ao menos 8 caracteres';
    return novos;
  }

  function aplicarErroApi(err: unknown) {
    if (err instanceof ApiException) {
      const mapa: Record<string, string> = {};
      for (const fe of err.apiError.fieldErrors) {
        mapa[fe.field] = fe.message;
      }
      // 401/409 nao trazem fieldErrors; cai na mensagem geral.
      if (Object.keys(mapa).length === 0) mapa.geral = err.apiError.message;
      setErros(mapa);
    } else {
      setErros({ geral: 'Não foi possível conectar. O back-end está rodando em :8080?' });
    }
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
      if (ehCadastro) {
        await registrar({ nome, email, senha });
      }
      // Apos cadastrar, ja autentica com as mesmas credenciais.
      await login({ email, senha });
      onAutenticado();
    } catch (err) {
      aplicarErroApi(err);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="auth-card">
      <div className="auth-tabs">
        <button
          type="button"
          className={`auth-tab ${!ehCadastro ? 'is-active' : ''}`}
          onClick={() => trocarModo('login')}
        >
          Entrar
        </button>
        <button
          type="button"
          className={`auth-tab ${ehCadastro ? 'is-active' : ''}`}
          onClick={() => trocarModo('cadastro')}
        >
          Criar conta
        </button>
      </div>

      <form className="prazo-form" onSubmit={handleSubmit} noValidate>
        <h2>{ehCadastro ? 'Criar conta' : 'Acessar'}</h2>

        {ehCadastro && (
          <label>
            Nome
            <input
              type="text"
              value={nome}
              onChange={(e) => setNome(e.target.value)}
              placeholder="Seu nome"
              autoComplete="name"
            />
            {erros.nome && <span className="erro-campo">{erros.nome}</span>}
          </label>
        )}

        <label>
          E-mail
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="voce@exemplo.com"
            autoComplete="email"
          />
          {erros.email && <span className="erro-campo">{erros.email}</span>}
        </label>

        <label>
          Senha
          <input
            type="password"
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            placeholder={ehCadastro ? 'Ao menos 8 caracteres' : 'Sua senha'}
            autoComplete={ehCadastro ? 'new-password' : 'current-password'}
          />
          {erros.senha && <span className="erro-campo">{erros.senha}</span>}
        </label>

        {erros.geral && <p className="erro-geral">{erros.geral}</p>}

        <button type="submit" className="btn btn-primary" disabled={enviando}>
          {enviando ? 'Aguarde...' : ehCadastro ? 'Criar conta e entrar' : 'Entrar'}
        </button>
      </form>
    </div>
  );
}
