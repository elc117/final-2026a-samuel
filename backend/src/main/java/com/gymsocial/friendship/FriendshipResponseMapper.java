package com.gymsocial.friendship;

import com.gymsocial.friendship.domain.FriendConnection;
import com.gymsocial.friendship.domain.IncomingFriendshipRequest;
import com.gymsocial.friendship.dto.FriendResponse;
import com.gymsocial.friendship.dto.FriendshipRequestResponse;
import com.gymsocial.shared.id.PublicIdCodec;
import com.gymsocial.shared.storage.ImageStorage;

public final class FriendshipResponseMapper {

    private final PublicIdCodec publicIdCodec;
    private final ImageStorage imageStorage;

    public FriendshipResponseMapper(
        PublicIdCodec publicIdCodec,
        ImageStorage imageStorage
    ) {
        this.publicIdCodec = publicIdCodec;
        this.imageStorage = imageStorage;
    }

    public FriendshipRequestResponse toRequestResponse(IncomingFriendshipRequest request) {
        return new FriendshipRequestResponse(
            request.id(),
            publicIdCodec.encode(request.requesterUserId()),
            request.requesterName(),
            request.requesterUsername(),
            createImageUrl(request.requesterImageKey()),
            request.createdAt().toString()
        );
    }

    public FriendResponse toFriendResponse(FriendConnection friend) {
        return new FriendResponse(
            publicIdCodec.encode(friend.userId()),
            friend.name(),
            friend.username(),
            createImageUrl(friend.profileImageKey()),
            friend.connectedAt().toString()
        );
    }

    private String createImageUrl(String imageKey) {
        return imageKey == null
            ? null
            : imageStorage.createReadUrl(imageKey);
    }
}
