package com.gymsocial.friendship.port;

import com.gymsocial.friendship.enums.AcceptResult;
import com.gymsocial.friendship.enums.RequestResult;
import com.gymsocial.friendship.domain.FriendshipPair;

import java.util.UUID;

public interface FriendshipCommandRepository {

    RequestResult request(FriendshipPair pair);

    AcceptResult accept(UUID friendshipId, long receiverUserId);

    boolean reject(UUID friendshipId, long receiverUserId);
}
