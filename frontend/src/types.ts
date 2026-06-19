export type StatusPrazo = 'PENDENTE' | 'CUMPRIDO';

export interface Prazo {
  id: number;
  numeroProcesso: string;
  descricao: string;
  dataPrazo: string;
  status: StatusPrazo;
  vencido: boolean;
  criadoEm: string;
  cumpridoEm: string | null;
  version: number;
}

export interface Pagina<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface CriarPrazoRequest {
  numeroProcesso: string;
  descricao: string;
  dataPrazo: string;
}

export interface AtualizarPrazoRequest {
  descricao: string;
  dataPrazo: string;
  version: number;
}

export interface ApiFieldError {
  field: string;
  message: string;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors: ApiFieldError[];
}
