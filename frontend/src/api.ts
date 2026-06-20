import type {
  ApiError,
  AtualizarPrazoRequest,
  CriarPrazoRequest,
  LoginRequest,
  Prazo,
  RegistrarRequest,
  TokenResponse,
  Usuario,
} from './types';

const API_BASE = 'http://localhost:8080';
const TOKEN_KEY = 'prazos.token';

export class ApiException extends Error {
  readonly apiError: ApiError;

  constructor(apiError: ApiError) {
    super(apiError.message);
    this.apiError = apiError;
  }
}

// --- Token (persistido no localStorage para sobreviver a refresh) ---

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function logout(): void {
  localStorage.removeItem(TOKEN_KEY);
}

// Avisa a aplicacao quando o token cai (expirou/invalido) para voltar ao login.
let onUnauthorized: (() => void) | null = null;

export function setOnUnauthorized(handler: () => void): void {
  onUnauthorized = handler;
}

// --- fetch com Authorization e tratamento padronizado de erro ---

function comAuth(headers: HeadersInit = {}): HeadersInit {
  const token = getToken();
  return token ? { ...headers, Authorization: `Bearer ${token}` } : headers;
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (response.status === 401) {
    logout();
    onUnauthorized?.();
  }
  if (!response.ok) {
    const body = (await response.json()) as ApiError;
    throw new ApiException(body);
  }
  return (await response.json()) as T;
}

// --- Autenticacao ---

export async function registrar(request: RegistrarRequest): Promise<Usuario> {
  return fetch(`${API_BASE}/auth/registrar`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  }).then((r) => handleResponse<Usuario>(r));
}

export async function login(request: LoginRequest): Promise<TokenResponse> {
  const token = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  }).then((r) => handleResponse<TokenResponse>(r));
  setToken(token.accessToken);
  return token;
}

// --- Prazos (rotas protegidas) ---

export function listarPrazos(): Promise<Prazo[]> {
  return fetch(`${API_BASE}/prazos`, {
    headers: comAuth(),
  }).then((r) => handleResponse<Prazo[]>(r));
}

export function criarPrazo(request: CriarPrazoRequest): Promise<Prazo> {
  return fetch(`${API_BASE}/prazos`, {
    method: 'POST',
    headers: comAuth({ 'Content-Type': 'application/json' }),
    body: JSON.stringify(request),
  }).then((r) => handleResponse<Prazo>(r));
}

export function atualizarPrazo(id: number, request: AtualizarPrazoRequest): Promise<Prazo> {
  return fetch(`${API_BASE}/prazos/${id}`, {
    method: 'PUT',
    headers: comAuth({ 'Content-Type': 'application/json' }),
    body: JSON.stringify(request),
  }).then((r) => handleResponse<Prazo>(r));
}

export function cumprirPrazo(id: number): Promise<Prazo> {
  return fetch(`${API_BASE}/prazos/${id}/cumprir`, {
    method: 'PATCH',
    headers: comAuth(),
  }).then((r) => handleResponse<Prazo>(r));
}
