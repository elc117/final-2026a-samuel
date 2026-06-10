package com.gymsocial.friendship.dto;

import java.time.Instant;

public record FriendResponse(
    String code,
    String name,
    String username,
    String profileImageUrl,
    Instant connectedAt
) {
}
