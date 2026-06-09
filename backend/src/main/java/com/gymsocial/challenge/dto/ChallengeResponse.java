package com.gymsocial.challenge.dto;

import com.gymsocial.challenge.Challenge;

import java.util.List;
import java.util.UUID;

public record ChallengeResponse(
    UUID id,
    UUID groupId,
    String title,
    String description,
    String period,
    boolean allowMultipleCheckInsPerDay,
    String startsAt,
    String endsAt,
    String status,
    List<ChallengeRankingResponse> ranking
) {

    public static ChallengeResponse from(
        Challenge challenge,
        List<ChallengeRankingResponse> ranking
    ) {
        return new ChallengeResponse(
            challenge.id(),
            challenge.groupId(),
            challenge.title(),
            challenge.description(),
            challenge.period(),
            challenge.allowMultipleCheckInsPerDay(),
            challenge.startsAt().toString(),
            challenge.endsAt().toString(),
            challenge.status(),
            ranking
        );
    }
}
