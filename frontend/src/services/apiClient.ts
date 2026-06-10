import { clearAccessToken, getAccessToken, setAccessToken } from "../features/auth/services/accessTokenStore";

const API_URL = import.meta.env.VITE_API_URL ?? "/api";
const DEFAULT_ERROR_MESSAGE = "Não foi possível concluir a solicitação.";
const AUTH_PATHS_WITHOUT_REFRESH = new Set(["/auth/login", "/auth/register", "/auth/refresh"]);

let refreshRequest: Promise<boolean> | null = null;

type ErrorResponse = {
  message?: string;
  errors?: Record<string, string>;
};

type RefreshResponse = {
  accessToken: string;
};

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
    public readonly errors: Record<string, string> = {},
  ) {
    super(message);
    this.name = "ApiError";
  }
}

function buildHeaders({ body, headers: customHeaders }: RequestInit) {
    const headers = new Headers(customHeaders);

    const shouldUseJson = body !== undefined && !(body instanceof FormData);

    const accessToken = getAccessToken();

  if (shouldUseJson && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  if (accessToken && !headers.has("Authorization")) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }

  return headers;
}

async function parseResponse<T>(response: Response): Promise<T> {
  if (response.status === 204) {
    return undefined as T;
  }

  return await response.json() as Promise<T>;
}

async function createApiError(response: Response): Promise<ApiError> {
  const body = await parseResponse<ErrorResponse | undefined>(response).catch(
    () => undefined,
  );

  const fieldMessage = body?.errors
    ? Object.values(body.errors)[0]
    : undefined;

  return new ApiError(
    response.status,
    fieldMessage ?? body?.message ?? DEFAULT_ERROR_MESSAGE,
    body?.errors,
  );
}

async function refreshAccessToken(): Promise<boolean> {
  if (!refreshRequest) {
    refreshRequest = fetch(`${API_URL}/auth/refresh`, {
      method: "POST",
      credentials: "include",
    })
      .then(async (response) => {
        if (!response.ok) {
          clearAccessToken();
          return false;
        }

        const body = await parseResponse<RefreshResponse>(response);
        setAccessToken(body.accessToken);
        return true;
      })
      .catch(() => {
        clearAccessToken();
        return false;
      })
      .finally(() => {
        refreshRequest = null;
      });
  }

  return refreshRequest;
}

export async function restoreSession(): Promise<boolean> {
  if (getAccessToken()) {
    return true;
  }

  return refreshAccessToken();
}

async function executeRequest<T>(path: string, options: RequestInit, allowRefresh: boolean): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    credentials: "include",
    headers: buildHeaders(options),
  });

  if (
    response.status === 401 &&
    allowRefresh &&
    !AUTH_PATHS_WITHOUT_REFRESH.has(path) &&
    await refreshAccessToken()
  ) {
    return executeRequest<T>(path, options, false);
  }

  if (!response.ok) {
    throw await createApiError(response);
  }

  return parseResponse<T>(response);
}

export function apiRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  return executeRequest<T>(path, options, true);
}

export function getApiErrorMessage(error: unknown): string {
  return error instanceof ApiError ? error.message : DEFAULT_ERROR_MESSAGE;
}
