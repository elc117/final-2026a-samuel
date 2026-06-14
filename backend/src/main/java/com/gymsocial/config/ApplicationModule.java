package com.gymsocial.config;

import com.gymsocial.shared.storage.MinioImageStorage;
import com.gymsocial.shared.id.PublicIdCodec;
import io.javalin.Javalin;

public final class ApplicationModule {

    private ApplicationModule() {
    }

    public static Javalin create(ApplicationConfig appConfig) {
        var dataSource = DatabaseConfig.createDataSource(appConfig);
        var imageStorage = new MinioImageStorage(appConfig);
        var publicIdCodec = new PublicIdCodec(appConfig.hashidsSalt());
        var auth = AuthModule.create(
            dataSource,
            appConfig,
            imageStorage,
            publicIdCodec
        );
        var group = GroupModule.create(
            dataSource,
            imageStorage,
            publicIdCodec
        );
        var groupInvitation = GroupInvitationModule.create(
            dataSource,
            imageStorage
        );
        var friendship = FriendshipModule.create(
            dataSource,
            imageStorage,
            publicIdCodec
        );
        var userProfile = UserProfileModule.create(
            dataSource,
            imageStorage,
            publicIdCodec,
            friendship.relationshipRepository()
        );
        var checkIn = CheckInModule.create(
            dataSource,
            imageStorage,
            publicIdCodec
        );
        var challenge = ChallengeModule.create(
            dataSource,
            imageStorage,
            publicIdCodec
        );
        var chat = ChatModule.create(
            dataSource,
            imageStorage,
            publicIdCodec
        );

        return JavalinConfig.create(
            appConfig,
            dataSource,
            auth,
            group,
            groupInvitation,
            userProfile,
            friendship.controller(),
            checkIn,
            challenge,
            chat
        );
    }
}
