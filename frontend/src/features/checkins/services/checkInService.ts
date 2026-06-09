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

export type CheckInComment = {
  id: string;
  authorUserId: number;
  authorName: string;
  authorImageUrl: string | null;
  content: string;
  createdAt: string;
};

export function getCheckIns(): Promise<CheckIn[]> {
  return apiRequest<CheckIn[]>("/check-ins");
}

export async function getCheckIn(checkInId: string): Promise<CheckIn> {
  const checkIns = await getCheckIns();
  const checkIn = checkIns.find((current) => current.id === checkInId);

  if (!checkIn) {
    throw new Error("Check-in não encontrado.");
  }

  return checkIn;
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

export function getCheckInComments(
  checkInId: string,
): Promise<CheckInComment[]> {
  return apiRequest<CheckInComment[]>(
    `/check-ins/${checkInId}/comments`,
  );
}

export function createCheckInComment(
  checkInId: string,
  content: string,
): Promise<CheckInComment> {
  return apiRequest<CheckInComment>(
    `/check-ins/${checkInId}/comments`,
    {
      method: "POST",
      body: JSON.stringify({ content }),
    },
  );
}
