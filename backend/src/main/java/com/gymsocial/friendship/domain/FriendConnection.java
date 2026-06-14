package com.gymsocial.friendship.domain;

import java.time.Instant;
import java.util.UUID;

public record FriendConnection(
    UUID friendshipId,
    long userId,
    String name,
    String username,
    String profileImageKey,
    Instant connectedAt
) {
}
