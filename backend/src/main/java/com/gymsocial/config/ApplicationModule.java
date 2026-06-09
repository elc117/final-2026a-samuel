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
        var groupController = GroupModule.create(
            dataSource,
            imageStorage,
            publicIdCodec
        );
        var groupInvitationController = GroupInvitationModule.create(
            dataSource,
            imageStorage
        );
        var userProfileController = UserProfileModule.create(
            dataSource,
            imageStorage,
            publicIdCodec
        );
        var checkIn = CheckInModule.create(
            dataSource,
            imageStorage,
            publicIdCodec
        );
        var challengeController = ChallengeModule.create(
            dataSource,
            imageStorage,
            publicIdCodec
        );

        return JavalinConfig.create(
            appConfig,
            dataSource,
            auth,
            groupController,
            groupInvitationController,
            userProfileController,
            checkIn,
            challengeController
        );
    }
}
