import { apiRequest } from "../../../services/apiClient";

export type GroupInvitation = {
  token: string;
  groupId: string;
  groupName: string;
  groupImageUrl: string | null;
  memberCount: number;
  maximumMembers: number;
  alreadyMember: boolean;
};

type GroupInviteLinkResponse = {
  token: string;
};

export async function getGroupInviteLink(
  groupId: string,
): Promise<string> {
  const response = await apiRequest<GroupInviteLinkResponse>(
    `/groups/${groupId}/invite-link`,
  );

  return `${window.location.origin}/convite/${response.token}`;
}

export function getGroupInvitation(
  token: string,
): Promise<GroupInvitation> {
  return apiRequest<GroupInvitation>(`/group-invitations/${token}`);
}

export function acceptGroupInvitation(token: string): Promise<void> {
  return apiRequest<void>(`/group-invitations/${token}/accept`, {
    method: "POST",
  });
}
