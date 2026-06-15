package com.gymsocial.friendship.dto;

public record FriendResponse(
    String code,
    String name,
    String username,
    String profileImageUrl,
    String connectedAt
) {
}
