import { apiRequest } from "../../../services/apiClient";

export type FriendshipRequest = {
  id: string;
  requesterCode: string;
  requesterName: string;
  requesterUsername: string;
  requesterImageUrl: string | null;
  createdAt: string;
};

type FriendshipRequestCountResponse = {
  count: number;
};

export const FRIENDSHIP_REQUESTS_CHANGED_EVENT =
  "friendship-requests-changed";

let countRequest: Promise<number> | null = null;

export function sendFriendshipRequest(userCode: string): Promise<void> {
  return apiRequest<void>(`/friendships/users/${userCode}`, {
    method: "POST",
  });
}

export function getFriendshipRequests(): Promise<FriendshipRequest[]> {
  return apiRequest<FriendshipRequest[]>("/friendships/requests");
}

export function getFriendshipRequestCount(force = false): Promise<number> {
  if (force) {
    countRequest = null;
  }

  if (!countRequest) {
    countRequest = apiRequest<FriendshipRequestCountResponse>(
      "/friendships/requests/count",
    )
      .then((response) => response.count)
      .finally(() => {
        countRequest = null;
      });
  }

  return countRequest;
}

export function notifyFriendshipRequestsChanged() {
  window.dispatchEvent(new Event(FRIENDSHIP_REQUESTS_CHANGED_EVENT));
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
