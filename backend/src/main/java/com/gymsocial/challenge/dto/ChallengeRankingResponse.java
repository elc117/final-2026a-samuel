package com.gymsocial.challenge.dto;

public record ChallengeRankingResponse(
    String userCode,
    String name,
    String profileImageUrl,
    int score
) {
}
