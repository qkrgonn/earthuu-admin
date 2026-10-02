const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "/api/admin/v1";

interface ApiEnvelope<T> {
  data: T;
}

interface ApiFieldError {
  field: string;
  message: string;
}

interface ApiErrorBody {
  code?: string;
  message?: string;
  errors?: ApiFieldError[];
}

interface CsrfResponse {
  headerName: string;
  token: string;
}

let csrfToken: CsrfResponse | null = null;

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
    public readonly code = "UNKNOWN_ERROR",
    public readonly errors: ApiFieldError[] = [],
  ) {
    super(message);
    this.name = "ApiError";
  }
}

async function issueCsrfToken(): Promise<CsrfResponse> {
  const response = await fetch(`${API_BASE_URL}/auth/csrf`, { credentials: "include" });
  if (!response.ok) throw new ApiError(response.status, "보안 토큰을 발급받지 못했습니다.");
  const envelope = await response.json() as ApiEnvelope<CsrfResponse>;
  csrfToken = envelope.data;
  return csrfToken;
}

function isUnsafe(method = "GET") {
  return !["GET", "HEAD", "OPTIONS"].includes(method.toUpperCase());
}

async function execute<T>(path: string, init: RequestInit, retry: boolean): Promise<T> {
  const method = init.method ?? "GET";
  const token = isUnsafe(method) ? (csrfToken ?? await issueCsrfToken()) : null;
  const headers = new Headers(init.headers);
  if (init.body) headers.set("Content-Type", "application/json");
  if (token) headers.set(token.headerName, token.token);

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    credentials: "include",
    headers,
  });
  if (response.status === 403 && retry && isUnsafe(method)) {
    csrfToken = null;
    await issueCsrfToken();
    return execute<T>(path, init, false);
  }
  if (!response.ok) {
    const body = await response.json().catch(() => null) as ApiErrorBody | null;
    throw new ApiError(response.status, body?.message ?? "요청을 처리하지 못했습니다.", body?.code, body?.errors);
  }
  if (response.status === 204) return undefined as T;
  const envelope = await response.json() as ApiEnvelope<T>;
  return envelope.data;
}

export async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  return execute<T>(path, init, true);
}

export function clearCsrfToken() {
  csrfToken = null;
}
