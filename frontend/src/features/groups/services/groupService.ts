import { apiRequest } from "../../../services/apiClient";

export type Group = {
  id: string;
  adminUserCode: string;
  name: string;
  imageUrl: string | null;
  memberCount: number;
  createdAt: string;
};

export type CreateGroupRequest = {
  name: string;
  image?: File;
};

export async function getCurrentGroup(): Promise<Group | null> {
  const group = await apiRequest<Group | undefined>("/groups/me");
  return group ?? null;
}

export function createGroup(
  request: CreateGroupRequest,
): Promise<Group> {
  const formData = new FormData();
  formData.set("name", request.name);

  if (request.image) {
    formData.set("image", request.image);
  }

  return apiRequest<Group>("/groups", {
    method: "POST",
    body: formData,
  });
}
