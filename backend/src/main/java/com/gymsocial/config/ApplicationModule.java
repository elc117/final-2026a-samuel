package com.gymsocial.config;

import io.javalin.Javalin;

public final class ApplicationModule {

    private ApplicationModule() {
    }

    public static Javalin create(ApplicationConfig appConfig) {
        var dataSource = DatabaseConfig.createDataSource(appConfig);
        var authController = AuthModule.create(dataSource, appConfig);

        return JavalinConfig.create(
            appConfig,
            dataSource,
            authController
        );
    }
}
