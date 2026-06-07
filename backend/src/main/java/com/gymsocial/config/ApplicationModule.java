package com.gymsocial.config;

import io.javalin.Javalin;

public final class ApplicationModule {

    private ApplicationModule() {
    }

    public static Javalin create(ApplicationConfig appConfig) {
        var dataSource = DatabaseConfig.createDataSource(appConfig);
        var auth = AuthModule.create(dataSource, appConfig);
        var groupController = GroupModule.create(dataSource);

        return JavalinConfig.create(
            appConfig,
            dataSource,
            auth,
            groupController
        );
    }
}
