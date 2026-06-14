package com.gymsocial.friendship.domain;

import java.time.Instant;
import java.util.UUID;

public record IncomingFriendshipRequest(
    UUID id,
    long requesterUserId,
    String requesterName,
    String requesterUsername,
    String requesterImageKey,
    Instant createdAt
) {
}
