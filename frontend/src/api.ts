import type { ApiError, AtualizarPrazoRequest, CriarPrazoRequest, Prazo } from './types';

const API_BASE = 'http://localhost:8080';

export class ApiException extends Error {
  readonly apiError: ApiError;

  constructor(apiError: ApiError) {
    super(apiError.message);
    this.apiError = apiError;
  }
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    throw new ApiException(await lerErro(response));
  }
  return (await response.json()) as T;
}

/** Lê o corpo de erro como ApiError; se não vier JSON (ex.: 500 com HTML), monta um equivalente. */
async function lerErro(response: Response): Promise<ApiError> {
  try {
    return (await response.json()) as ApiError;
  } catch {
    return {
      timestamp: new Date().toISOString(),
      status: response.status,
      error: response.statusText,
      message: 'Erro inesperado ao comunicar com o servidor.',
      path: '',
      fieldErrors: [],
    };
  }
}

/**
 * Converte um erro de API no mapa { campo: mensagem } usado pelos formulários.
 * Erros sem campo específico (ex.: 409 duplicado) e falhas de rede caem em `geral`.
 */
export function mapearErrosDeCampo(err: unknown): Record<string, string> {
  if (err instanceof ApiException) {
    if (err.apiError.fieldErrors.length > 0) {
      return Object.fromEntries(err.apiError.fieldErrors.map((fe) => [fe.field, fe.message]));
    }
    return { geral: err.apiError.message };
  }
  return { geral: 'Não foi possível salvar. O back-end está rodando?' };
}

export function listarPrazos(): Promise<Prazo[]> {
  return fetch(`${API_BASE}/prazos`).then((r) => handleResponse<Prazo[]>(r));
}

export function criarPrazo(request: CriarPrazoRequest): Promise<Prazo> {
  return fetch(`${API_BASE}/prazos`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  }).then((r) => handleResponse<Prazo>(r));
}

export function atualizarPrazo(id: number, request: AtualizarPrazoRequest): Promise<Prazo> {
  return fetch(`${API_BASE}/prazos/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  }).then((r) => handleResponse<Prazo>(r));
}

export function cumprirPrazo(id: number): Promise<Prazo> {
  return fetch(`${API_BASE}/prazos/${id}/cumprir`, {
    method: 'PATCH',
  }).then((r) => handleResponse<Prazo>(r));
}
