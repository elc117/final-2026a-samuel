import { apiRequest } from "../../../services/apiClient";

export type AuthUser = {
  id: number;
  name: string;
  username: string;
  email: string;
  profileImageUrl: string | null;
  createdAt: string;
};

export type AuthResponse = {
  user: AuthUser;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type RegisterRequest = {
  name: string;
  username: string;
  email: string;
  password: string;
};

export function login(request: LoginRequest): Promise<AuthResponse> {
  return apiRequest<AuthResponse>("/auth/login", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export function register(request: RegisterRequest): Promise<AuthResponse> {
  return apiRequest<AuthResponse>("/auth/register", {
    method: "POST",
    body: JSON.stringify(request),
  });
}
