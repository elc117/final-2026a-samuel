package com.gymsocial.config;

import com.gymsocial.friendship.FriendshipController;
import com.gymsocial.friendship.FriendshipResponseMapper;
import com.gymsocial.friendship.FriendshipService;
import com.gymsocial.friendship.database.FriendshipCommandRepository;
import com.gymsocial.friendship.database.FriendshipQueryRepository;
import com.gymsocial.friendship.database.JdbcTransactionManager;
import com.gymsocial.friendship.port.FriendshipRelationshipRepository;
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
        var commandRepository = new FriendshipCommandRepository(
            dataSource,
            new JdbcTransactionManager(dataSource)
        );
        var queryRepository = new FriendshipQueryRepository(dataSource);
        var service = new FriendshipService(
            commandRepository,
            queryRepository,
            new FriendshipResponseMapper(publicIdCodec, imageStorage),
            publicIdCodec
        );
        return new Components(
            new FriendshipController(service),
            queryRepository
        );
    }

    public record Components(
        FriendshipController controller,
        FriendshipRelationshipRepository relationshipRepository
    ) {
    }
}
