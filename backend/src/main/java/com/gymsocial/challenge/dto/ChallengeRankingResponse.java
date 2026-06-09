package com.gymsocial.challenge.dto;

public record ChallengeRankingResponse(
    long userId,
    String name,
    String profileImageUrl,
    int score
) {
}
