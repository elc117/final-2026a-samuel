import { apiRequest } from "../../../services/apiClient";

export type FriendshipRequest = {
  id: string;
  requesterCode: string;
  requesterName: string;
  requesterUsername: string;
  requesterImageUrl: string | null;
  createdAt: string;
};

export function sendFriendshipRequest(userCode: string): Promise<void> {
  return apiRequest<void>(`/friendships/users/${userCode}`, {
    method: "POST",
  });
}

export function getFriendshipRequests(): Promise<FriendshipRequest[]> {
  return apiRequest<FriendshipRequest[]>("/friendships/requests");
}

export function acceptFriendshipRequest(requestId: string): Promise<void> {
  return apiRequest<void>(`/friendships/${requestId}/accept`, {
    method: "POST",
  });
}

export function rejectFriendshipRequest(requestId: string): Promise<void> {
  return apiRequest<void>(`/friendships/${requestId}/reject`, {
    method: "POST",
  });
}
