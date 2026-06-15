package com.gymsocial.friendship.dto;

import java.util.UUID;

public record FriendshipRequestResponse(
    UUID id,
    String requesterCode,
    String requesterName,
    String requesterUsername,
    String requesterImageUrl,
    String createdAt
) {
}
