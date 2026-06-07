import { apiRequest } from "../../../services/apiClient";

export type Group = {
  id: string;
  adminUserId: number;
  name: string;
  imageUrl: string | null;
  memberCount: number;
  createdAt: string;
};

export type CreateGroupRequest = {
  name: string;
  imageUrl?: string;
};

export async function getCurrentGroup(): Promise<Group | null> {
  const group = await apiRequest<Group | undefined>("/groups/me");
  return group ?? null;
}

export function createGroup(
  request: CreateGroupRequest,
): Promise<Group> {
  return apiRequest<Group>("/groups", {
    method: "POST",
    body: JSON.stringify(request),
  });
}
