import { apiRequest } from "../../../services/apiClient";

export type ChallengePeriod =
  | "WEEKLY"
  | "QUARTERLY"
  | "SEMIANNUAL"
  | "ANNUAL"
  | "CUSTOM";

export type ChallengeRanking = {
  userCode: string;
  name: string;
  profileImageUrl: string | null;
  score: number;
};

export type Challenge = {
  id: string;
  groupId: string;
  title: string;
  description: string | null;
  period: ChallengePeriod;
  allowMultipleCheckInsPerDay: boolean;
  startsAt: string;
  endsAt: string;
  status: "ACTIVE" | "ENDED";
  ranking: ChallengeRanking[];
};

export type CreateChallengeRequest = {
  title: string;
  description?: string;
  period: ChallengePeriod;
  endsAt?: string;
  allowMultipleCheckInsPerDay: boolean;
};

export async function getCurrentChallenge(): Promise<Challenge | null> {
  const challenge = await apiRequest<Challenge | undefined>(
    "/challenges/current",
  );
  return challenge ?? null;
}

export function createChallenge(
  request: CreateChallengeRequest,
): Promise<Challenge> {
  return apiRequest<Challenge>("/challenges", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export function endCurrentChallenge(): Promise<void> {
  return apiRequest<void>("/challenges/current", {
    method: "DELETE",
  });
}
