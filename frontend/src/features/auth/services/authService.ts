import { apiRequest } from "../../../services/apiClient";
import {
  clearAccessToken,
  setAccessToken,
} from "./accessTokenStore";

export type AuthUser = {
  id: number;
  name: string;
  username: string;
  email: string;
  profileImageUrl: string | null;
  createdAt: string;
};

export type AuthResponse = {
  accessToken: string;
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

export async function login(request: LoginRequest): Promise<AuthResponse> {
  const response = await apiRequest<AuthResponse>("/auth/login", {
    method: "POST",
    body: JSON.stringify(request),
  });

  setAccessToken(response.accessToken);
  return response;
}

export async function register(
  request: RegisterRequest,
): Promise<AuthResponse> {
  const response = await apiRequest<AuthResponse>("/auth/register", {
    method: "POST",
    body: JSON.stringify(request),
  });

  setAccessToken(response.accessToken);
  return response;
}

export function getCurrentUser(): Promise<AuthUser> {
  return apiRequest<AuthUser>("/auth/me");
}

export async function logout(): Promise<void> {
  try {
    await apiRequest<void>("/auth/logout", {
      method: "POST",
    });
  } finally {
    clearAccessToken();
  }
}
