package com.gymsocial.friendship.port;

import com.gymsocial.friendship.domain.FriendConnection;
import com.gymsocial.friendship.domain.IncomingFriendshipRequest;
import com.gymsocial.shared.pagination.InstantUuidCursor;
import java.util.List;

public interface FriendshipQueryRepository
    extends FriendshipRelationshipRepository {

    List<IncomingFriendshipRequest> findIncomingRequests(long receiverUserId);

    int countIncomingRequests(long receiverUserId);

    List<FriendConnection> findFriendsPage(
        long userId,
        InstantUuidCursor cursor,
        int limit
    );
}
