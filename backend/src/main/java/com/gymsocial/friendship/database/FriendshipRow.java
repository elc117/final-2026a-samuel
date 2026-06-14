package com.gymsocial.friendship.database;

import com.gymsocial.friendship.enums.FriendshipStatus;

import java.util.UUID;

public record FriendshipRow(
    UUID id,
    long requesterUserId,
    long receiverUserId,
    FriendshipStatus status
) {
}
