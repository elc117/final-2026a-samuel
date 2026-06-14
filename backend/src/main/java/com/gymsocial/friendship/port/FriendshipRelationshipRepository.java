package com.gymsocial.friendship.port;

import com.gymsocial.friendship.enums.Relationship;

public interface FriendshipRelationshipRepository {

    Relationship findRelationship(long viewerUserId, long targetUserId);
}
