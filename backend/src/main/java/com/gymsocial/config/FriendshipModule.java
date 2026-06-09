package com.gymsocial.config;

import com.gymsocial.friendship.FriendshipController;
import com.gymsocial.friendship.FriendshipRepository;
import com.gymsocial.friendship.FriendshipService;
import com.gymsocial.shared.id.PublicIdCodec;
import com.gymsocial.shared.storage.ImageStorage;

import javax.sql.DataSource;

public final class FriendshipModule {

    private FriendshipModule() {
    }

    public static Components create(
        DataSource dataSource,
        ImageStorage imageStorage,
        PublicIdCodec publicIdCodec
    ) {
        var repository = new FriendshipRepository(dataSource);
        var service = new FriendshipService(
            repository,
            publicIdCodec,
            imageStorage
        );
        return new Components(
            new FriendshipController(service),
            repository
        );
    }

    public record Components(
        FriendshipController controller,
        FriendshipRepository repository
    ) {
    }
}
