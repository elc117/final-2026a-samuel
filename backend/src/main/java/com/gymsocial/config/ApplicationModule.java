package com.gymsocial.config;

import com.gymsocial.shared.storage.MinioImageStorage;
import io.javalin.Javalin;

public final class ApplicationModule {

    private ApplicationModule() {
    }

    public static Javalin create(ApplicationConfig appConfig) {
        var dataSource = DatabaseConfig.createDataSource(appConfig);
        var imageStorage = new MinioImageStorage(appConfig);
        var auth = AuthModule.create(dataSource, appConfig, imageStorage);
        var groupController = GroupModule.create(dataSource, imageStorage);
        var groupInvitationController = GroupInvitationModule.create(
            dataSource,
            imageStorage
        );
        var userProfileController = UserProfileModule.create(
            dataSource,
            imageStorage
        );
        var checkIn = CheckInModule.create(dataSource, imageStorage);
        var challengeController = ChallengeModule.create(
            dataSource,
            imageStorage
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
