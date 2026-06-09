import { apiRequest } from "../../../services/apiClient";

export type UserProfile = {
  code: string;
  name: string;
  username: string;
  profileImageUrl: string | null;
  friendCount: number;
};

export type UpdateProfileRequest = {
  name: string;
  image?: File;
};

export function getProfile(): Promise<UserProfile> {
  return apiRequest<UserProfile>("/users/me");
}

export function getPublicProfile(userCode: string): Promise<UserProfile> {
  return apiRequest<UserProfile>(`/users/${userCode}`);
}

export function updateProfile(request: UpdateProfileRequest): Promise<UserProfile> {
  const formData = new FormData();
  formData.set("name", request.name);

  if (request.image) {
    formData.set("image", request.image);
  }

  return apiRequest<UserProfile>("/users/me", {
    method: "PUT",
    body: formData,
  });
}
