const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:7000";
const DEFAULT_ERROR_MESSAGE = "Não foi possível concluir a solicitação.";

type ErrorResponse = {
  message?: string;
};

export class ApiError extends Error {
  constructor(public readonly status: number, message: string) {
    super(message);
    this.name = "ApiError";
  }
}

function buildHeaders({ body, headers: customHeaders }: RequestInit) {
  const headers = new Headers(customHeaders);
  const shouldUseJson = body && !(body instanceof FormData);

  if (shouldUseJson && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
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

  return new ApiError(
    response.status,
    body?.message ?? DEFAULT_ERROR_MESSAGE,
  );
}

export async function apiRequest<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    credentials: "include",
    headers: buildHeaders(options),
  });

  if (!response.ok) {
    throw await createApiError(response);
  }

  return parseResponse<T>(response);
}
