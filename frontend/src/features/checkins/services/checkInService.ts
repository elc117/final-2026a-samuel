import { apiRequest } from "../../../services/apiClient";
import type { CursorPage } from "../../../shared/pagination/CursorPage";

export type CheckIn = {
  id: string;
  groupId: string;
  authorCode: string;
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
  authorCode: string;
  authorName: string;
  authorImageUrl: string | null;
  content: string;
  createdAt: string;
};

export type CheckInPage = CursorPage<CheckIn>;

const pageRequests = new Map<string, Promise<CheckInPage>>();

export function getCheckIns(
  cursor?: string | null,
  limit = 10,
): Promise<CheckInPage> {
  const params = new URLSearchParams({ limit: String(limit) });
  if (cursor) {
    params.set("cursor", cursor);
  }
  const path = `/check-ins?${params.toString()}`;
  const currentRequest = pageRequests.get(path);
  if (currentRequest) {
    return currentRequest;
  }

  const request = apiRequest<CheckInPage>(path).finally(() => {
    pageRequests.delete(path);
  });
  pageRequests.set(path, request);
  return request;
}

export function getCheckIn(checkInId: string): Promise<CheckIn> {
  return apiRequest<CheckIn>(`/check-ins/${checkInId}`);
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
