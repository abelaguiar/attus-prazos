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
    const body = (await response.json()) as ApiError;
    throw new ApiException(body);
  }
  return (await response.json()) as T;
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
