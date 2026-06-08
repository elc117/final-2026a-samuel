import { apiRequest } from "../../../services/apiClient";

export type CheckIn = {
  id: string;
  groupId: string;
  authorUserId: number;
  authorName: string | null;
  authorImageUrl: string | null;
  title: string;
  description: string | null;
  imageUrl: string;
  createdAt: string;
};

export type CreateCheckInRequest = {
  title: string;
  description?: string;
  image: File;
};

export function getCheckIns(): Promise<CheckIn[]> {
  return apiRequest<CheckIn[]>("/check-ins");
}

export function createCheckIn(
  request: CreateCheckInRequest,
): Promise<CheckIn> {
  const formData = new FormData();
  formData.set("title", request.title);
  formData.set("description", request.description ?? "");
  formData.set("image", request.image);

  return apiRequest<CheckIn>("/check-ins", {
    method: "POST",
    body: formData,
  });
}
